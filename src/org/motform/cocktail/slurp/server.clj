(ns org.motform.cocktail.slurp.server
  (:require [mount.core         :as mount]
            [reitit.dev.pretty  :as pretty]
            [reitit.ring        :as ring]
            [ring.adapter.jetty :as jetty]
            [reitit.ring.middleware.exception  :as exception]
            [reitit.ring.middleware.parameters :as parameters]
            [org.motform.cocktail.slurp.view   :as view]))

(def app
  (ring/ring-handler
   (ring/router
    [["/"
      {:name ::home
       :doc  "Home page, what the kids might call a 'feed'."
       :get  (fn [{:keys [query-params]}]
               {:status 200
                :body   (view/cocktails (query-params "cursor") :home)})}]

     ["/cocktail/{id}"
      {:name ::cocktail
       :doc  "A single cocktail view."
       :get  (fn [{{:keys [id]} :path-params}]
               {:status 200
                :body (view/cocktail id)})}]

     ["/cocktails"
      {:name ::cocktails
       :doc  "The primary cocktail card grid."
       :get  (fn [request]
               {:status 200
                :body   (view/cocktails
                         (select-keys request [:query-params :query-string])
                         :strainer)})}]]

    {:exception pretty/exception
     :data {:middleware [parameters/parameters-middleware
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
  )
