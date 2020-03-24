(ns cocktail.spit.routes
  (:require [bidi.bidi :as bidi]
            [re-frame.core :as rf]
            [pushy.core :as pushy]))

(def routes ["/" {"" :cocktails
                  "menu" :menu
                  "library" :library
                  "about" :about
                  ["cocktail/" :id] :cocktail}])

(defn- parse-url [url]
  (bidi/match-route routes url))

(defn- dispatch-route [matched-route]
  (let [page (:handler matched-route)]
    (if-let [id (get-in matched-route [:route-params :id])]
      (rf/dispatch [:cocktail-by-id id]))
    (rf/dispatch [:active-page page])))

(defn app-routes []
  (pushy/start! (pushy/pushy dispatch-route parse-url)))

(def url-for (partial bidi/path-for routes))

(defn cocktail-url-for [id]
  (bidi/path-for routes :cocktail :id id))
