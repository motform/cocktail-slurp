(ns cocktail-slurp.routes
  (:require [bidi.ring :refer [make-handler]]
            [ring.util.response :as ring]
            [muuntaja.core :as m]
            [cocktail-slurp.db :as db]))

(defn- transit+json-response [data]
  (-> (m/encode "application/transit+json" data)
      (ring/response)
      (ring/header "Content-Type" "application/transit+json")))

(defn strain-handler [{:keys [body]}]
  (let [strainer (m/decode "application/transit+json" body)]
    (transit+json-response (db/cocktail-by-title (:cocktail strainer)))))

(defn cocktail-handler [{:keys [params]}]
  (transit+json-response (db/cocktail-by-id (get params "id"))))

(defn random-cocktails [{:keys [params]}]
  (let [cocktails (-> params (get "cocktails") (Integer/parseInt))]
    (transit+json-response (db/random-cocktails cocktails))))

(def route-handler
  (make-handler ["/" {"index.html" :TODO-index
                      "bartender/" {"strain" strain-handler
                                    "cocktail" cocktail-handler
                                    "cocktails/" {"random" random-cocktails}}}]))
