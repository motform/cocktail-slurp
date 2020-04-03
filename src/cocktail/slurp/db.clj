(ns cocktail.slurp.db
  (:require [clojure.string :as str]
            [cocktail.slurp.parse :as parse]
            [cocktail.stuff.util :as util]
            [datomic.api :as d]))

;;; db

(defonce *conn (atom nil))

(defn- init-conn! [uri]
  (reset! *conn (d/connect uri)))

(defn init-db!
  "Initialize and populate the in-memory Datomic db.
   Expects `posts` to be the relative path to an .edn of scraped posts."
  [{:keys [uri schema posts]}]
  (d/create-database uri)
  (init-conn! uri)
  @(d/transact @*conn (-> schema slurp read-string))
  @(d/transact @*conn (parse/posts->cocktails posts)))

;;; queries

(defn paginate
  "Simple functional pagination of query results.
   Returns nil on fail, for use with `when` or reagent."
  [start limit q & args]
  (let [result (apply q args)
        stop (+ start limit)]
    (when (seq result)
      (if (< stop (count result))
        (subvec result start stop)
        (subvec result start)))))

(defn all
  "Lists all `v` for `a`."
  [a]
  (into #{} (d/q [:find '[?a ...]
                  :where ['_ a '?a]]
                 (d/db @*conn))))

(defn cocktail-by-id [id]
  (d/pull (d/db @*conn) '[:id :title :recipe :preparation :ingredients] [:id id]))

(defn cocktail-feed []
  (let [result (d/q '[:find [(pull ?e [:date :id :title :recipe :preparation :ingredients]) ...]
                      :where [?e :id]]
                    (d/db @*conn))]
    (->> result (sort-by :date compare) reverse (into []))))

(def base-query
  '{:query {:find [(pull ?e [:id :title :recipe :preparation :ingredients])]
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

(defn append-* [strings]
  (map #(str % \*) strings))

(defn- wash-strainer
  "Homogenize & split strings, possibly do other processing to input"
  [{:keys [search type ingredients] :as strainer}]
  (cond-> strainer
    search (update :search #(-> % str/lower-case str/trim (str/replace #" +" " ") (str/split #" ") append-*))))

(defn- parse-strainer
  "Builds a query map based on user input, excepts irrelevant keys to be falsy.
   Order of clause conj is preserved, so try and do fulltext last."
  [{:keys [:ingredients :search :type]}]
  (cond-> base-query
    type (and-query :type \t type)
    ingredients (and-query :ingredients \i ingredients)
    search (fn-and-query :fulltext 'fulltext \f '$ search)))

(defn strain [strainer]
  (let [{:keys [query args]} (-> strainer util/remove-empty wash-strainer parse-strainer)]
    (if (seq args) ; handle stupid empty calls -> move to interceptor
      (into [] (map first (apply d/q query (d/db @*conn) args)))
      (cocktail-feed))))



(comment
  ;; datomic
  (init-db! {:uri "datomic:mem://cocktail.slurp/repl"
             :posts "posts.edn"
             :schema "resources/edn/cocktail-schema.edn"})

  (d/delete-database "datomic:mem://cocktail.slurp/repl")

  (strain {:ingredients ["rum" "cream"] :search ["russian"]})

  ;; export the cocktails
  (spit "resources/edn/formatted-posts.edn" (pr-str (parse/posts->cocktails "posts.edn"))))
