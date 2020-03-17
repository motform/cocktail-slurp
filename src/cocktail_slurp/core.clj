(ns cocktail-slurp.core
  (:gen-class)
  (:require [cocktail-slurp.routes :as routes]
            [cocktail-slurp.db :as db]
            [muuntaja.middleware :refer [wrap-format]]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.flash :refer [wrap-flash]]))

(defn wrap-body-string [handler]
  (fn [request]
    (handler (if (:body request)
               (assoc request :body (->> request :body .bytes slurp))
               request))))

;; TODO clean up the handler
(def bartender
  (-> #'routes/route-handler
      (wrap-cors :access-control-allow-origin  [#"http://localhost:8020"]
                 :access-control-allow-methods [:post :get])
      wrap-body-string
      wrap-session
      wrap-params
      wrap-flash
      wrap-reload))

(defn -main []
  (db/init-db! "posts.edn")
  (println "init")
  (run-server bartender {:port 3232}))
