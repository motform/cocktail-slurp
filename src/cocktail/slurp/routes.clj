(ns cocktail.slurp.routes
  (:require [cocktail.slurp.db :as db]))

;; TODO test all routes
(def bartender
  [["/all/{attribute}"
    {:name :bartender/all
     :doc "Returns all values for that :db/attribute."
     :parameters {:path [:map [:attribute string?]]} ;; TODO spec
     :get (fn [{{:keys [attribute]} :path-params}]
            {:status 200
             :body (db/all (keyword attribute))})}]

   ["/strain"
    {:name :bartender/strain
     :doc "Parses and reruns a vector of cocktails filtered by the strainer."
     ;; :parameters {:query [:map]} ;; TODO spec
     :post (fn [{strainer :body-params}] ;; TODO 
             {:status 200
              :body (if-let [cocktails (db/paginate 0 20 db/strain strainer)]
                      cocktails
                      [])})}]

   ["/cocktail/{id}"
    {:name :bartender/cocktail
     :doc "Get a cocktail by its id."
     :parameters {:path [:map [:id string?]]}
     :get (fn [{{:keys [id]} :path-params}]
            {:status 200
             :body (db/cocktail-by-id id)})}]

   ;; NOTE not in use - use /strain instead
   ["/cocktails"
    {:name :bartender/cocktails
     :doc "Get default cocktail feed paginated by `start` and `end`, 
           sorted by date added."
     :post (fn [{{:keys [start end]} :query-params}]
             {:status 200
              :body (db/paginate start end db/cocktail-feed)})}]])
