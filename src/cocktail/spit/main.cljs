(ns cocktail.spit.main
  (:require [reagent.dom :as r]
            [re-frame.core :as rf]
            [cocktail.spit.routes :as routes]
            [cocktail.spit.events :as event]
            [cocktail.spit.subs :as sub]
            [day8.re-frame.http-fx]))

(def debug? ^boolean goog.DEBUG)

(defn dev-setup []
  (when debug?
    (enable-console-print!)))

(defn app []
  (let [current-route @(rf/subscribe [::sub/route])]
    [:<>
     (when current-route [(get-in current-route [:data :view])])
     [:footer "quality versus quantity does not have to be a winner-take-all proposition"]]))

(defn render []
  (routes/init-routes!)
  (r/render [app]
            (.getElementById js/document "mount")))

(defn ^:dev/after-load clear-cache-and-render! []
  (rf/clear-subscription-cache!)
  (render))

(defn ^:export mount []
  (rf/dispatch-sync [:db/initialize])
  (rf/dispatch-sync [:meta/all "ingredients"])
  (dev-setup)
  (render))
