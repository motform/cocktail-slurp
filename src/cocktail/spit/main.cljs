(ns cocktail.spit.main
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.dom :as gdom]
            [cocktail.spit.routes :as routes]
            [cocktail.spit.components.app :as app]
            [cocktail.spit.events]
            [cocktail.spit.subs]
            [day8.re-frame.http-fx]))

;; (devtools/install!)

(enable-console-print!) 

(defn render []
  (r/render [app/main]
            (gdom/getElement "mount")))

(defn ^:dev/after-load clear-cache-and-render! []
  (rf/clear-subscription-cache!)
  (render))

(defn ^:export mount []
  (routes/app-routes)
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch-sync [:meta-all "ingredients"])
  (render))
