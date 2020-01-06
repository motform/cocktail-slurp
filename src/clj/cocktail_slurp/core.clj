(ns cocktail-slurp.core
  (:require [cocktail-slurp.parse :as parse]))

(def posts
  (->> "resources/posts.edn" slurp read-string (pmap parse/post->map) drop-last))

