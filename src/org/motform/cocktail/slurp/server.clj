(ns org.motform.cocktail.slurp.server
  (:require [mount.core         :as mount]
            [reitit.dev.pretty  :as pretty]
            [clojure.data.json  :as json]
            [reitit.ring        :as ring]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.cookies           :as cookies]
            [reitit.ring.middleware.exception  :as exception]
            [reitit.ring.middleware.parameters :as parameters]
            [org.motform.cocktail.slurp.db     :as db]
            [org.motform.cocktail.slurp.view   :as view]))

(def app
  (ring/ring-handler
   (ring/router
    [["/"
      {:name ::home
       :doc  "Home page, what the kids might call a 'feed'."
       :get  (fn [request]
               {:status 200
                :body   (view/cocktails (select-keys request [:query-params :query-string :cookies])
                                        :home)})}]

     ["/set-cookie"
      {:name  ::set-cookie
       :doc   "Sets a cookie."
       :post  (fn [{{:strs [view]} :form-params {:strs [referer]} :headers}]
                {:status  301
                 :cookies {"view" {:value view}}
                 :headers {"location" referer}})}]

     
     ["/api/possible-ingredients"
      {:get (fn [{{:strs [ingredient]} :query-params
                  :keys  [query-string]}]
              {:status  200
               :headers {"content-type" "application/json"}
               :body
               (let [ingredients (-> (if ingredient ingredient []) vector flatten)]
                 (json/write-str {"ingredients"    (db/enumerated-possible-ingredients ingredients)
                                  "cocktailCards"  (str (view/ajax-cocktail-cards
                                                         {:query-params {"search" ""
                                                                         "ingredient" ingredients}
                                                          :query-string query-string
                                                          :cookies {}}
                                                         :strainer))}))})}]

     ["/spill/{id}" ; this should really be a post, but the css for input-submit did not want to rotate
      {:name ::spill
       :doc  "Retracts cocktail from db."
       :get  (fn [{{:keys [id]} :path-params}]
               (db/retract-cocktail id "Cocktail retracted by cocktail-page button.")
               {:status  301
                :headers {"location" "/"}})}]

     ["/favorite"
      {:name ::favorite
       :doc  "Toggle cocktail as favorite in db."
       :post  (fn [{{:strs [id]} :form-params}]
                (db/toggle-cocktail-favorite id "Cocktail toggled by cocktail-page form.")
                {:status  303
                 :headers {"location" (str "/cocktail/" id)}})}]

     ["/cocktail/{id}"
      {:name ::cocktail
       :doc  "A single cocktail view."
       :get  (fn [{{:keys [id]} :path-params}]
               {:status 200
                :body   (view/cocktail id)})}]

     ["/cocktails"
      {:name ::cocktails
       :doc  "The primary cocktail card grid."
       :get  (fn [request]
               {:status 200
                :body   (view/cocktails
                         (select-keys request [:query-params :query-string :cookies])
                         :strainer)})}]]

    {:exception pretty/exception
     :data {:middleware [parameters/parameters-middleware
                         cookies/wrap-cookies
                         exception/exception-middleware]}})
   (ring/routes
    (ring/create-resource-handler {:path "/"})
    (ring/redirect-trailing-slash-handler)
    (ring/create-default-handler))))

(mount/defstate server
  :start (jetty/run-jetty #'app {:port 8888 :join? false})
  :stop  (.stop server))

(comment
  (app {:request-method :get
        :uri ""})

  stranier-ingredients
  stranier-query
  {:query-params {"search" "", "ingredient" ["bourbon whiskey" "rye whiskey"]}, :query-string "search=&ingredient=bourbon+whiskey&ingredient=rye+whiskey", :cookies {}}  
  )
