(ns cocktail.slurp.db
  (:require [cocktail.slurp.parse :as parse]
            [datomic.api :as d]
            [cocktail.slurp.db :as db]))

;;; db

(def *conn (atom nil))

(defn init-conn! [uri]
  (reset! *conn (d/connect uri)))

(defn init-db!
  "Initialize and populate the in-memory Datomic db.
   Expects `posts` to be the relative path to an .edn of scraped posts"
  [{:keys [uri schema posts]}]
  (d/create-database uri)
  (init-conn! uri)
  @(d/transact @*conn (-> schema slurp read-string))
  @(d/transact @*conn (parse/posts->cocktails posts)))

;;; queries

(defn paginate [index limit q & args]
  {:pre [(pos? limit)]}
  (subvec (apply (memoize q) args) index (+ index limit)))

(defn cocktail-by
  "Factory for creating single attribute quires."
  [a]
  (fn [v]
    (d/q '[:find [(pull ?e [*]) ...]
           :in $ ?a ?v
           :where [?e ?a ?v]]
         (d/db @*conn) a v)))

(def cocktail-by-id #(ffirst ((cocktail-by :id) %)))
(def cocktail-by-title (cocktail-by :title))

;; TODO move to pull api, implement pagination
(defn cocktail-feed [n]
  (let [cocktails (d/q '[:find (pull ?e [:id :title :recipie :preparation :ingredients])
                         :where [?e :id]]
                       (d/db @*conn))]
    (->> cocktails (take n) (mapv first))))
(defn cocktail-by-fulltext [search]
  (d/q '[:find [(pull ?e [:id :title :recipie]) ...]
         :in $ ?search
         :where [(fulltext $ :fulltext ?search) [[?e ?n]]]]
       (d/db @*conn) search))

(comment
  ;; datomic
  (init-db! {:uri "datomic:mem://cocktail.slurp"
             :posts "posts.edn"
             :schema "resources/edn/cocktail-schema.edn"})
  (d/delete-database "datomic:mem://cocktail.slurp")

  (d/q '[:find (pull ?e [:id :title])
         :where
         [?e :id ?id]]
       (d/db @*conn))

  ;; export the cocktails
  (spit "resources/edn/formatted-posts.edn" (pr-str (parse/posts->cocktails "posts.edn"))))
