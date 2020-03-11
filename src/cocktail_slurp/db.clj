(ns cocktail-slurp.db
  (:require [cocktail-slurp.parse :as parse]))

(def posts
  (->> "resources/edn/posts.edn" slurp read-string (pmap parse/post->map) drop-last))

(defn cocktail-by [k]
  (fn [x]
    (->> posts (filter #(= x (k %))) first)))

(def cocktail-by-title (cocktail-by :title))

(def cocktail-by-id (cocktail-by :id))

(defn random-cocktails [n]
  (->> posts (random-sample 0.01) (take n) (into #{})))

#_(spit "resources/edn/formatted-posts.edn" (pr-str posts))
