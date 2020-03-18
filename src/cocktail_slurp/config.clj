(ns cocktail-slurp.config
  (:require [clojure.edn :as edn]))

(defn dev []
  (-> (try (slurp "env/dev.edn") (catch Exception _ nil))
      (edn/read-string)))

(def env
  (dev))
