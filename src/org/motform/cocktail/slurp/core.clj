(ns org.motform.cocktail.slurp.core
  (:gen-class)
  (:require [mount.core :as mount]
            [org.motform.cocktail.slurp.db]
            [org.motform.cocktail.slurp.server]))

(defn -main [_]
  (mount/start))

(comment
  (-main nil)
  )
