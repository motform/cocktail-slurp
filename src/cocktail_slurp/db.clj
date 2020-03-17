(ns cocktail-slurp.db
  (:require [cocktail-slurp.parse :as parse]
            [datomic.api :as d]))

;;; db

(def uri "datomic:mem://cocktail-slurp")

(def *conn (atom nil))

;; TODO refactor data into cocktail/ author/ ingredient/ bar/ and category/ namespaces
(def schema
  [{:db/ident :id
    :db/doc "(Blogspot) id of the cocktail."
    :db/valueType :db.type/string :db/cardinality :db.cardinality/one :db/unique :db.unique/identity}
   {:db/ident :title
    :db/doc "Cocktail title. Note that we allow for/have multiple cocktails with the same title."
    :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :date
    :db/doc "Date when the cocktail was posted, in YYMMDD."
    :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :author
    :db/doc "Author, should be replaced with a ?v pointing to an ?e."
    :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :recipie
    :db/doc "List of ingredients required for the cocktail."
    :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :preparation
    :db/doc "Instructions on how to mix the cocktail."
    :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :story
    :db/doc "Everything else from the post body."
    :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :img
    :db/doc "URL for the img from Blogspot, not always available."
    :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :url
    :db/doc "URL to the original post."
    :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :bars
    :db/doc "Bars tagged in the post."
    :db/valueType :db.type/string :db/cardinality :db.cardinality/many}
   {:db/ident :categories
    :db/doc "Categories tagged in the post."
    :db/valueType :db.type/string :db/cardinality :db.cardinality/many}
   {:db/ident :ingredients
    :db/doc "Ingredients used in the cocktail."
    :db/valueType :db.type/string :db/cardinality :db.cardinality/many}])

(defn init-conn! []
  (reset! *conn (d/connect uri)))

(defn init-db!
  "Initialize and populate the in-memory Datomic db.
   Expects `posts` to be the relative path to an .edn of scraped posts"
  [posts]
  (d/create-database uri)
  (init-conn!)
  @(d/transact @*conn schema)
  @(d/transact @*conn (parse/posts->cocktails posts)))

;;; queries

(defn cocktail-by
  "Factory for creating single attribute-single result quires."
  [a]
  (fn [v]
    (d/q '[:find (pull ?e [*])
           :in $ ?a ?v
           :where [?e ?a ?v]]
         (d/db @*conn) a v)))

(def cocktail-by-id (cocktail-by :id))
(def cocktail-by-title (cocktail-by :title))

;; TODO move to pull api, implement pagination, shrink payload
(defn cocktail-feed [n]
  (let [cocktails (d/q '[:find (pull ?e [*])
                         :where [?e :id]]
                       (d/db @*conn))]
    (->> cocktails (take n) (mapv first))))

(comment
  ;; datomic
  (init-db! "posts.edn")
  (d/delete-database uri)

  ;; export the cocktails
  (spit "resources/edn/formatted-posts.edn" (pr-str (parse/posts->cocktails "posts.edn"))))
