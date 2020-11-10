(ns cocktail.slurp.server
  (:gen-class)
  (:require [cocktail.slurp.db :as db]
            [cocktail.slurp.routes :as routes]
            [cocktail.stuff.config :as config]
            [muuntaja.core :as m]
            [reitit.coercion.malli :as malli]
            [reitit.dev.pretty :as pretty]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coersion]
            [reitit.ring.middleware.dev :as dev]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.spec :as spec]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.cors :as cors]))

(def server
  (ring/ring-handler
   (ring/router

    [""
     ["/bartender" routes/bartender]] ;; TODO routes!

    {:exception pretty/exception
     :coercion malli/coercion
     :validate spec/validate
     :reitit.middleware/transform dev/print-request-diffs ;; TODO add debug switch
     :data {:muuntaja m/instance
            :middleware [[cors/wrap-cors
                          :access-control-allow-origin [#"http://localhost:8020"]
                          :access-control-allow-methods [:get :put :post :delete]]
                         parameters/parameters-middleware
                         muuntaja/format-negotiate-middleware
                         muuntaja/format-response-middleware
                         coersion/coerce-response-middleware
                         exception/exception-middleware
                         coersion/coerce-exceptions-middleware
                         muuntaja/format-request-middleware
                         muuntaja/format-middleware
                         coersion/coerce-request-middleware]}})

   (ring/routes
    (ring/create-resource-handler {:path "/"})
    (ring/redirect-trailing-slash-handler)
    (ring/create-default-handler
     {:not-found ; delegate routing to the front end
      (fn [_] {:status 200 :body (slurp "resources/public/index.html")})}))))

(defn -main []
  (let [{:keys [datomic http]} config/env]
    (println "init, creating database…")
    (db/init-db! datomic)
    (println (str "database created, listening on http://localhost:" (:port http)))
    (jetty/run-jetty #'server {:port (:port http) :join? false})))

(comment
  (let [{:keys [datomic]} config/env]
    (db/init-db! datomic))

  (server {:request-method :get
           :uri "/bartender/all/ingredients"})

  (server {:request-method :get
           :uri "/bartender/cocktail/4459043163866934583"})

  (server {:request-method :get
           :uri "/bartender/cocktail/4459043163866934583"})

  )
