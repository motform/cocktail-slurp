(ns cocktail-slurp.db
  (:require [cocktail-slurp.parse :as parse]
            [datahike.api :as d]))

(def cocktails
  (->> "posts.edn" ;; TODO replace file once its clean
      slurp
      read-string
      (pmap parse/post->cocktail)
      (filter parse/cocktail?)
      (into []))) ; could be made into a transducer

(defn cocktail-by [k]
  (fn [x]
    (->> cocktails (filter #(= x (k %))) first)))

(def cocktail-by-title (cocktail-by :title))
(def cocktail-by-id (cocktail-by :id))

(defn random-cocktails [n]
  (->> cocktails (random-sample 0.01) (take n) (into #{})))

(def uri "datahike:file:///tmp/cocktail-slurp")

(def schema [{:db/ident :id          :db/valueType :db.type/string :db/cardinality :db.cardinality/one :db/unique :db.unique/identity}
             {:db/ident :title       :db/valueType :db.type/string :db/cardinality :db.cardinality/one :db/unique :db.unique/identity}
             {:db/ident :date        :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
             {:db/ident :author      :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
             {:db/ident :story       :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
             {:db/ident :recipie     :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
             {:db/ident :preparation :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
             {:db/ident :img         :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
             {:db/ident :url         :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
             {:db/ident :bar         :db/valueType :db.type/string :db/cardinality :db.cardinality/many}
             {:db/ident :category    :db/valueType :db.type/string :db/cardinality :db.cardinality/many}
             {:db/ident :ingredient  :db/valueType :db.type/string :db/cardinality :db.cardinality/many}])

(comment
  ;; datahike

  (d/create-database uri)

  (def conn (d/connect uri))

  (d/transact conn schema)

  (d/transact conn (into [] cocktails)) ; has to be in a vector!

  (d/q '[:find ?title ?img
         :where
         [?id :title ?title]
         [?id :img ?img]
         [?id :ingredients "rum"]
         [?id :ingredients "egg"]]
       @conn)

  (d/delete-database uri)

  ;; export the cocktails
  (spit "resources/edn/formatted-posts.edn" (pr-str cocktails)))
