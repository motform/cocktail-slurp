(ns cocktail.slurp.routes
  (:require [bidi.ring :refer [make-handler]]
            [ring.util.response :as ring]
            [muuntaja.core :as m]
            [cocktail.slurp.db :as db]))

(defn- transit+json-response [data]
  (-> (m/encode "application/transit+json" data)
      (ring/response)
      (ring/header "Content-Type" "application/transit+json")))

(defn- home-page [_]
  (ring/file-response "index.html" {:root "resources/public"}))

(defn- all [{:keys [params]}]
  (let [a (-> params (get "attribute") keyword)]
    (transit+json-response (db/all a))))

(defn- strain [{:keys [body]}]
  (let [strainer (m/decode "application/transit+json" body)
        result (db/paginate 0 20 db/strain strainer)]
    (transit+json-response (map first result))))

(defn- cocktail-by-id [{:keys [params]}]
  (transit+json-response (db/cocktail-by-id (params "id"))))

(defn- cocktail-feed [{:keys [params]}]
  (let [start (-> params (get "start") (Integer/parseInt))
        end (-> params (get "end") (Integer/parseInt))]
    (transit+json-response (db/paginate start end db/cocktail-feed))))

(def route-handler
  (make-handler
   ["/" {"" home-page
         "index.html" home-page
         "bartender/" {"all" all
                       "strain" strain
                       "cocktail" cocktail-by-id
                       "cocktails" cocktail-feed}}]))
