(ns org.motform.cocktail.slurp.server
  (:gen-class)
  (:require [muuntaja.core :as m]
            [mount.core :as mount]
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
            [org.motform.cocktail.slurp.route :as route]))

(def app
  (ring/ring-handler
   (ring/router
    route/routes
    {:exception pretty/exception
     ;; :reitit.middleware/transform dev/print-request-diffs ;; TODO add debug switch
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
  (do
    (mount/stop  #'org.motform.cocktail.slurp.server/server)
    (mount/start #'org.motform.cocktail.slurp.server/server))


  (app {:request-method :get
        :uri "/ping/"})
  
  (app {:request-method :get
        :uri ""})

  (app {:request-method :get
        :uri "/bartender/cocktail/4459043163866934583"})

  (app {:request-method :get
        :uri "/bartender/cocktail/4459043163866934583"})

  )
