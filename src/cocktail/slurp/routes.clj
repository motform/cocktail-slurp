(ns cocktail.slurp.routes
  (:require [bidi.ring :refer [make-handler]]
            [ring.util.response :as ring]
            [muuntaja.core :as m]
            [cocktail.slurp.db :as db]))

(defn- transit+json-response [data]
  (-> (m/encode "application/transit+json" data)
      (ring/response)
      (ring/header "Content-Type" "application/transit+json")))

(defn strain-handler [{:keys [body]}]
  (let [strainer (m/decode "application/transit+json" body)]
    (transit+json-response (db/cocktail-by-title (:cocktail strainer)))))

(defn cocktail-handler [{:keys [params]}]
  (transit+json-response (db/cocktail-by-id (params "id"))))

(defn cocktail-feed [{:keys [params]}]
  (let [cocktails (-> params (get "cocktails") (Integer/parseInt))]
    (transit+json-response (db/cocktail-feed cocktails))))

(defn home-page [_]
  (ring/file-response "index.html" {:root "resources/public"}))

(def route-handler
  (make-handler ["/" {"" home-page
                      "index.html" home-page
                      ;; "" (ring/resource-response "resources/public/index.html")
                      "bartender/" {"strain" strain-handler
                                    "cocktail" cocktail-handler
                                    "cocktails/" {"feed" cocktail-feed}}}]))
