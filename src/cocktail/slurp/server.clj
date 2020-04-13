(ns cocktail.slurp.server
  (:gen-class)
  (:require [cocktail.slurp.db :as db]
            [cocktail.slurp.routes :as routes]
            [cocktail.stuff.config :as config]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.session :refer [wrap-session]]))

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
    (println (str "database created, listening on http://localhost:" (:port http)))
    (run-server bartender {:port (:port http)})))
