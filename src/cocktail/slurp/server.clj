(ns cocktail.slurp.server
  (:gen-class)
  (:require [cocktail.slurp.db :as db]
            [cocktail.slurp.routes :as routes]
            [cocktail.stuff.config :as config]
            [org.httpkit.server :refer [run-server]]
            [ring.logger :as logger]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.session :refer [wrap-session]]))

(defn- wrap-body-string [handler]
  (fn [request]
    (handler (if (:body request)
               (assoc request :body (->> request :body .bytes slurp))
               request))))

;; TODO clean up the handler
(def bartender
  (-> #'routes/route-handler
      logger/wrap-log-response
      (wrap-resource "public")
      wrap-not-modified
      wrap-body-string
      wrap-session
      wrap-params
      wrap-reload
      wrap-flash
      logger/wrap-log-request-start))

(defn -main []
  (let [{:keys [datomic http]} config/env]
    (println "init, creating database…")
    (db/init-db! datomic)
    (println (str "database created, listening on http://localhost:" (:port http)))
    (run-server bartender {:port (:port http)})))
