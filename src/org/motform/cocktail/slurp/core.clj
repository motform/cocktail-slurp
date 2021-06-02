(ns org.motform.cocktail.slurp.core
  (:require [mount.core :as mount]
            [org.motform.cocktail.slurp.db]
            [org.motform.cocktail.slurp.server]
            [org.motform.cocktail.slurp.scrape])
  (:gen-class))

(defn -main [_]
  (mount/start))

(comment
  (-main nil)
  )
