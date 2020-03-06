(ns cocktail-slurp.core
  (:gen-class)
  (:require [cocktail-slurp.routes :as routes]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.flash :refer [wrap-flash]]))

(defn wrap-body-string
  "from https://stackoverflow.com/a/37397991"
  [handler]
  (fn [request]
    (handler (assoc request :body (->> request :body .bytes slurp)))))

;; TODO clean up the handler
(def bartender
  (-> #'routes/route-handler
      wrap-body-string
      (wrap-cors :access-control-allow-origin  [#"http://localhost:8020"]
                 :access-control-allow-methods [:post :put :get :delete])
      wrap-session
      wrap-params
      wrap-flash
      wrap-reload))

(defn -main []
  (println "init")
  (run-server bartender {:port 3232}))
