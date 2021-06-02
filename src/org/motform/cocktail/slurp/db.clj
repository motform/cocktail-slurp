(ns org.motform.cocktail.slurp.db
  (:require [clojure.string :as str]
            [datomic.api    :as d]
            [mount.core     :as mount]
            [org.motform.cocktail.slurp.parse :as parse]
            [org.motform.cocktail.stuff.util  :as util]))

;;; db

(defn init-db
  "Initialize and populate the in-memory Datomic db.
   Expects `posts` to be the relative path to an .edn of scraped posts."
  [{:keys [schema posts uri]}]
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (d/transact conn (-> schema slurp read-string))
    (d/transact conn (-> posts slurp read-string parse/posts->cocktails))
    conn))

(mount/defstate conn
  :start (init-db {:uri    "datomic:mem://cocktail.slurp/dev-server"
                   :schema "resources/edn/cocktail-schema.edn"
                   :posts  "posts.edn"})
  :stop (d/shutdown false))

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
  (d/pull (d/db conn)
          '[*]
          [:cocktail/id id]))

(defn cocktail-feed []
  (let [result (d/q '[:find [(pull ?e [:cocktail/date :cocktail/id :cocktail/title :cocktail/recipe :cocktail/preparation :cocktail/ingredient]) ...]
                      :where [?e :cocktail/id]]
                    (d/db conn))]
    (->> result (sort-by :cocktail/date compare) reverse (into []))))

(def base-query
  '{:query {:find [(pull ?e [:cocktail/id :cocktail/title :cocktail/recipe :cocktail/preparation :cocktail/ingredient :cocktail/kind])]
            :in [$]
            :where []}
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
  (let [{:keys [search kind ingredient] :as strainer} (util/keywordize strainer)]
    (cond-> strainer
      (not (str/blank? search)) (update :search #(-> % str/lower-case str/trim (str/replace #" +" " ") (str/split #" ") append-*))
      (util/?coll? ingredient) (update :ingredient #(conj [] %))
      (util/?coll? kind)       (update :kind #(conj [] %)))))

(defn- parse-strainer
  "Builds a query map based on user input, excepts irrelevant keys to be falsy.
   Order of clause conj is preserved, so try and do fulltext last."
  [{:keys [ingredient search kind]}]
  (cond-> base-query
    kind       (and-query :cocktail/kind \k kind)
    ingredient (and-query :cocktail/ingredient \i ingredient)
    search     (fn-and-query :cocktail/fulltext 'fulltext \f '$ search))) ; put `search` in vec so we can reuse the same fn

(defn strain [strainer]
  (let [{:keys [query args]} (-> strainer util/remove-empty wash-strainer parse-strainer)]
    (if (seq args) ; handle stupid empty calls -> move to interceptor
      (into [] (map first (apply d/q query (d/db conn) args)))
      (->> cocktail-feed (drop 250) (into [])))))

(comment
  ;; datomic
  (init-db {:uri "datomic:mem://cocktail.slurp/repl"
            :posts "posts.edn"
            :schema "resources/edn/cocktail-schema.edn"})

  (d/delete-database "datomic:mem://cocktail.slurp/repl")

  (strain {:ingredient "rum" :kind "shaken" :search "russian"}) ; strainer supports both str and [str]

  (all :cocktail/ingredient)
  ;; export the cocktails
  (spit "resources/edn/formatted-posts.edn" (pr-str (parse/posts->cocktails "posts.edn"))))
