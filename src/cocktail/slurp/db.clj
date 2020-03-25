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

;; NOTE returns nil and not [] on fail
(defn paginate [start limit q & args]
  (let [result (apply (memoize q) args)
        stop (+ start limit)]
    (when (seq result)
      (if (< stop (count result))
        (subvec result start stop)
        (subvec result start)))))

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

(defn cocktail-by-ingredients [ingredients]
  (d/q '[:find [(pull ?e [*]) ...]
         :in $ [?ingredients ...]
         :where
         [?e :ingredients ?ingredients]]
       (d/db @*conn) ingredients))

(defn cocktail-feed []
  (d/q '[:find [(pull ?e [:id :title :recipie :preparation :ingredients]) ...]
         :where [?e :id]]
       (d/db @*conn)))

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
