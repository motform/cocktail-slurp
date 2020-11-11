(ns cocktail.spit.routes
  (:require [cocktail.spit.events :as event]
            [cocktail.spit.components.cocktail :as cocktail]
            [cocktail.spit.components.cocktails :as cocktails]
            [re-frame.core :as rf]
            [reitit.coercion.spec :as reitit.spec]
            [reitit.frontend :as reitit.frontend]
            [reitit.frontend.easy :as reitit.easy]))

(def routes
  ["/" 
   [""
    {:name :route/cocktails
     :view cocktails/main}]

   ["cocktail/:id"
    {:name :route/cocktail
     :view cocktail/main
     :controllers [{:parameters {:path [:id]}
                    :start (fn [{{:keys [id]} :path}]
                             (rf/dispatch [:cocktail/by-id id])
                             (rf/dispatch [:cocktail/set-title]))}]}]])

(def router
  (reitit.frontend/router
   routes
   {:data {:coersion reitit.spec/coercion}}))

(defn on-navigate [new-match]
  (if new-match
    (rf/dispatch [::event/navigated new-match])
    (rf/dispatch [::event/navigate :route/cocktails])))

(defn init-routes! []
  (reitit.easy/start!
   router
   on-navigate
   {:use-fragment false}))
