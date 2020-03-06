(ns cocktail-slurp.db
  (:require [cocktail-slurp.parse :as parse]))

(def posts
  (->> "resources/edn/posts.edn" slurp read-string (pmap parse/post->map) drop-last))

(defn cocktail-by-id [id]
  (filter #(= id (:id %)) posts))

(defn cocktail-by-title [title]
  (filter #(= title (:title %)) posts))

#_(spit "resources/edn/formatted-posts.edn" (pr-str posts))
