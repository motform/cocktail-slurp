(ns cocktail-slurp.db
  (:require [cocktail-slurp.parse :as parse]))

(def cocktails
  (->> "resources/edn/posts.edn"
      slurp
      read-string
      (pmap parse/post->cocktail)
      (filter parse/cocktail?)))

(defn cocktail-by [k]
  (fn [x]
    (->> cocktails (filter #(= x (k %))) first)))

(def cocktail-by-title (cocktail-by :title))
(def cocktail-by-id (cocktail-by :id))

(defn random-cocktails [n]
  (->> cocktails (random-sample 0.01) (take n) (into #{})))

(comment
  ;; export the cocktails
  (spit "resources/edn/formatted-posts.edn" (pr-str cocktails)))
