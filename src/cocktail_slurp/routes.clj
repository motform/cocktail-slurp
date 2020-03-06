(ns cocktail-slurp.routes
  (:require [bidi.ring :refer [make-handler]]
            [muuntaja.core :as m]
            [cocktail-slurp.db :as db]))

(defn- transit+json-response [data & status]
  {:status (or status 200)
   :headers {"Content-Type" "application/transit+json"}
   :body (m/encode "application/transit+json" data)})

(defn strain-handler [request]
  (let [strainer (m/decode "application/transit+json" (:body request))]
    (transit+json-response (db/cocktail-by-title (:cocktail strainer)))))

(def route-handler
  (make-handler ["/" {"strain" strain-handler}]))
