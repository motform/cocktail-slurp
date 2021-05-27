(ns org.motform.cocktail.slurp.route
  (:require [org.motform.cocktail.slurp.view :as view]))

(def routes
  [["/"
    {:name ::home
     :doc  "Home page, what the kids might call a 'feed'."
     :get  (fn [_]
             {:status 200
              :body   (view/home {})})}]

   ["/cocktail/{id}"
    {:name ::cocktail
     :doc  "A single cocktail view."
     :get  (fn [{{:keys [id]} :path-params}]
             {:status 200
              :body (view/cocktail id)})}]

   ["/cocktails"
    {:name ::cocktails
     :doc  "The primary cocktail card grid."
     :get (fn [{:keys [query-params]}]
            {:status 200
             :body   (view/cocktails query-params)})}]])

;; [["/all/{attribute}"
;;   {:name :bartender/all
;;    :doc "Returns all values for that :db/attribute."
;;    :parameters {:path [:map [:attribute string?]]} ;; TODO spec
;;    :get (fn [{{:keys [attribute]} :path-params}]
;;           {:status 200
;;            :body (db/all (keyword "cocktail" attribute))})}]

;;  ["/strain"
;;   {:name :bartender/strain
;;    :doc "Parses a strainer used to filter and reruns a vector of cocktails."
;;    ;; :parameters {:query [:map]} ;; TODO spec
;;    :post (fn [{{:keys [cursor amount] :or {cursor 0 amount 20} :as strainer} :body-params}]
;;            {:status 200
;;             :body (db/paginate cursor amount db/strain (dissoc strainer :cursor :amount))})}]

;;  ["/cocktail/{id}"
;;   {:name :bartender/cocktail
;;    :doc "Get a cocktail by its id."
;;    :parameters {:path [:map [:id string?]]}
;;    :get (fn [{{:keys [id]} :path-params}]
;;           {:status 200
;;            :body (db/cocktail-by-id id)})}]]
