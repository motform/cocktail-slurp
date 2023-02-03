(ns org.motform.cocktail.slurp.db
  (:require [clojure.edn     :as edn]
            [clojure.java.io :as io]
            [clojure.string  :as str]
            ;; [datomic.api     :as d]
            [datascript.core    :as d]
            [mount.core      :as mount]
            [org.motform.cocktail.slurp.parse :as parse]
            [org.motform.cocktail.stuff.util  :as util])
  (:import (java.io PushbackReader)))

;;; db

(defn- read-edn-resource [path]
  (with-open [reader (io/reader (io/resource path))]
    (edn/read (PushbackReader. reader))))

(def bar (read-edn-resource "edn/bar.edn"))

;; (defn init-db
;;   "Initialize and populate the in-memory Datomic db.
;;    Expects `posts` to be the relative path to an .edn of scraped posts."
;;   [{:keys [schema posts uri]}]
;;   (println "Initializing DB at" uri)
;;   (d/create-database uri)
;;   (let [conn (d/connect uri)]
;;     (println "Reading and transacting" schema)
;;     (d/transact conn (read-edn-resource schema))
;;     (println "Reading and transacting" posts)
;;     (d/transact conn (-> posts read-edn-resource parse/posts->cocktails))
;;     (println "Database initalized")
;;     conn))

(def schema
  {:cocktail/id {:db/cardinality :db.cardinality/one
                 :db/unique :db.unique/identity}
   :cocktail/ingredient {:db/cardinality :db.cardinality/many}})

(defn init-db
  "Initialize and populate the in-memory Datomic db.
   Expects `posts` to be the relative path to an .edn of scraped posts."
  [{:keys [posts]}]
  (let [conn   (d/create-conn schema)
        posts  (parse/posts->cocktails (read-edn-resource posts))]
    (d/transact conn posts)
    conn))

(mount/defstate conn
  :start (init-db {;; :uri    "datomic:mem://cocktail.slurp/dev-server"
                   ;; :schema "edn/cocktail-schema.edn"
                   :posts  "edn/posts.edn"})
  :stop nil)

(defn add-cocktail [conn cocktail]
  (d/transact conn [cocktail]))

;;; queries

(defn paginate
  "Returns `amount` of whatever `q` yields when called with `args`
   counting from zero-indexed `start`, along with a :cursor."
  [cursor amount q & args]
  (let [result (apply q args)
        next   (+ cursor amount)
        len    (count result)
        stop   (if (>= len next) next (+ cursor (- len cursor)))]
    {:cocktails (util/?subvec result cursor stop) :cursor next :end? (not (>= len next))}))

(defn all
  "Lists all `v` for `a`."
  [a]
  (into #{} (d/q [:find '[?a ...]
                  :where ['_ a '?a]]
                 (d/db conn))))

(defn cocktail-by-id [id]
  (let [result (d/pull (d/db conn) '[*] [:cocktail/id id])]
    (when (:db/id result) result))) ; if the cocktail is missing, :db/id is nil

(defn cocktail-by-title
  "NOTE: Return first scalar, regardless of name conflicts."
  [title]
  (d/q '[:find ?id .
         :in $ ?title
         :where
         [?e :cocktail/title ?title]
         [?e :cocktail/id ?id]]
       (d/db conn) title))

(defn cocktail-feed []
  (let [result (d/q '[:find [(pull ?e [*]) ...]
                      :in $ ?collection
                      :where
                      [?e :cocktail/id]
                      [?e :cocktail/ingredient ?ingredient]
                      [(contains? ?collection ?ingredient)]]
                    (d/db conn) bar)]
    (->> result (sort-by :cocktail/date) reverse (into []))))

(def base-query
  '{:query {:find  [(pull ?e [*])]
            :in    [$ ?collection]
            :where [[?e :cocktail/ingredient ?ingredient]
                    [(contains? ?collection ?ingredient)]]}
    :args []})

(defn- gen-syms [s xs]
  (map-indexed (fn [i _] (symbol (str \? s i))) xs))

(defn- gen-where [e k syms]
  (for [s syms] [e k s]))

(defn- gen-fn-where [f db k syms]
  (for [s syms] [`(~f ~db ~k ~s) '[[?e ?n]]]))

(defn- and-query
  "Example input: {q} :title \t [t1 t2...]
   Output: :in [?t4 ...] :where [?e :title ?t1] :args [t1 t2...]"
  [q k s xs]
  (let [syms (gen-syms s xs)]
    (-> q
        (update-in [:query :in] concat syms)
        (update-in [:query :where] concat (gen-where '?e k syms))
        (update-in [:args] concat xs))))

(defn- fn-and-query
  "Example input: {q} :title \t 'fulltext '$ [t1 t2...]
   Output: :in [?1 ...] :where [(fulltext $ :title ?t1) [[?e ?n]]] :args [t1 t2...]"
  [q k f s db xs]
  (let [syms (gen-syms s xs)]
    (-> q
        (update-in [:query :in] concat syms)
        (update-in [:query :where] concat (gen-fn-where f db k syms))
        (update-in [:args] concat xs))))

(defn- append-* [strings]
  (map #(str % \*) strings))

(defn- wash-strainer
  "Homogenize & split strings, possibly do other processing to input."
  [strainer]
  (let [{:keys [search kind ingredient favorites] :as strainer} (util/keywordize strainer)]
    (cond-> strainer
      favorites                 (update :favorites boolean)
      (not (str/blank? search)) (update :search #(-> % str/lower-case str/trim (str/replace #" +" " ") (str/split #" ") append-*))
      (util/?coll? ingredient)  (update :ingredient #(conj [] %))
      (util/?coll? kind)        (update :kind #(conj [] %)))))

(defn- parse-strainer
  "Builds a query map based on user input, excepts irrelevant keys to be falsy.
   Order of clause conj is preserved, so try and do fulltext last."
  [{:keys [ingredient search kind favorites]}]
  (cond-> base-query
    kind       (and-query :cocktail/kind       \k kind)
    ingredient (and-query :cocktail/ingredient \i ingredient)
    favorites  (and-query :user/favorite       \f [favorites])
    search     (fn-and-query :cocktail/fulltext 'fulltext \s '$ search))) ; put `search` in vec so we can reuse the same fn

(defn strain [strainer]
  (let [{:keys [query args]} (-> strainer util/remove-empty wash-strainer parse-strainer)]
    (if (seq args) ; if we are on the home page
      (mapv first (apply d/q query (d/db conn) bar args))
      (cocktail-feed))))

(defn retract-cocktail [id reason]
  (d/transact conn [[:db.fn/retractEntity [:cocktail/id id]]
                    [:db/add "datomic.tx" :db/doc reason]]))

(defn toggle-cocktail-favorite [id reason]
  (d/transact conn [[:db/add [:cocktail/id id]
                     :user/favorite (-> id cocktail-by-id :user/favorite not)]
                    [:db/add "datomic.tx" :db/doc reason]]))

(defn possible-ingredients [ingredients]
  (let [{:keys [query args]}
        (and-query '{:query {:find  [[?i ...]]
                             :where [[?e :cocktail/ingredient ?i]]
                             :in    [$]}
                     :args []}
                   :cocktail/ingredient \i ingredients)]
    (apply d/q query (d/db conn) args)))

(defn enumerated-possible-ingredients [ingredients]
  (let [{:keys [query args]}
        (and-query '{:query {:find  [(pull ?e [:cocktail/ingredient])]
                             :where [[?e :cocktail/ingredient ?ingredient]
                                     [(contains? ?collection ?ingredient)]]
                             :in    [$ ?collection]}
                     :args []}
                   :cocktail/ingredient \i ingredients)]
    (->> (apply d/q query (d/db conn) bar args)
         (mapcat (comp :cocktail/ingredient first))
         frequencies)))

(comment

  (def conn (init-db {:posts  "edn/posts.edn"}))

  (d/q '{:find [[?i ...]]
         :where [[?e :cocktail/ingredient ?i0]
                 [?e :cocktail/ingredient ?i2]
                 [?e :cocktail/ingredient ?i]]
         :in [$ ?i0 ?i1]}
       (d/db conn)
       "genever"
       "cream")

  (->> (d/q '{:find [(pull ?e [:cocktail/ingredient])]
              :where
              [[?e :cocktail/ingredient ?i0]
               [?e :cocktail/ingredient ?i2]
               [?e :cocktail/ingredient ?i]]
              :in [$ ?i0 ?i1]}
            (d/db conn)
            "genever"
            "cream")
       (mapcat (comp :cocktail/ingredient first))
       frequencies)

  (d/q '{:find [[?i ...]]
         :where [[?e :cocktail/ingredient ?i0]
                 [?e :cocktail/ingredient ?i2]
                 [?e :cocktail/ingredient ?i]]
         :in [$ ?i0 ?i1]}
       (d/db conn)
       "genever"
       "cream")

  (d/q '[:find  ?t
         :where
         [?e :cocktail/ingredient "genever"]
         [?e :cocktail/ingredient "cream"]
         [?e :cocktail/title ?t]]
       (d/db conn))

;; datomic
  (init-db {:uri    "datomic:mem://cocktail.slurp/repl"
            :posts  "resources/edn/posts.edn"
            :schema "resources/edn/cocktail-schema.edn"})

  (d/delete-database "datomic:mem://cocktail.slurp/repl")

  ;; strainer supports both str and [str]
  (strain {:ingredient "rum"
           :kind       "shaken"
           :search     "russian"})

  (toggle-cocktail-favorite "5576108970359620518" "testing")

  (:user/favorite (cocktail-by-id "7857488667133973271"))

  (all :cocktail/kind)

  (cocktail-by-id "7536913279937580588")

  ;; export the cocktails
  (spit "resources/edn/formatted-posts.edn" (pr-str (parse/posts->cocktails "resources/edn/posts.edn")))

  :comment)

