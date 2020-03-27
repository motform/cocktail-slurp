(ns cocktail.slurp.server
  (:gen-class)
  (:require [cocktail.stuff.config :as config]
            [cocktail.slurp.db :as db]
            [cocktail.slurp.routes :as routes]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.resource :refer [wrap-resource]]))

(set! *warn-on-reflection* 1)

;; TODO refactor, remove reflection from .bytes
(defn- wrap-body-string [handler]
  (fn [request]
    (handler (if (:body request)
               (assoc request :body (->> request :body .bytes slurp))
               request))))

;; TODO clean up the handler
(def bartender
  (-> #'routes/route-handler
      (wrap-cors :access-control-allow-origin  [#"http://localhost:8020"]
                 :access-control-allow-methods [:post :get])
      (wrap-resource "public")
      wrap-not-modified
      wrap-body-string
      wrap-session
      wrap-params
      wrap-reload
      wrap-flash))

(defn -main []
  (let [{:keys [datomic http]} config/env]
    (println "init, creating database…")
    (db/init-db! datomic)
    (println "database created, listening on http://localhost:" (:port http))
    (run-server bartender {:port (:port http)})))
