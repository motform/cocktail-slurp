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

(defn- strain [{:keys [body]}]
  (let [strainer (m/decode "application/transit+json" body)]
    (transit+json-response (db/paginate 0 20 db/strain strainer))))

(defn- cocktail-by-id [{:keys [params]}]
  (transit+json-response (db/cocktail-by-id (params "id"))))

(defn- cocktail-feed [{:keys [params]}]
  (let [start (-> params (get "start") (Integer/parseInt))
        end (-> params (get "end") (Integer/parseInt))]
    (transit+json-response (db/paginate start end db/cocktail-feed))))

(defn- fulltext [{:keys [params]}]
  (transit+json-response (db/cocktail-by-fulltext (get params "search"))))

(def route-handler
  (make-handler
   ["/" {"" home-page
         "index.html" home-page
         "bartender/" {"strain" strain
                       "cocktail" cocktail-by-id
                       "cocktails/" {"feed" cocktail-feed
                                     "fulltext" fulltext}}}]))
