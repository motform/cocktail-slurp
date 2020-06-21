(ns cocktail.spit.components.cocktails
  (:require [cocktail.spit.components.catalouge :as catalouge]
            [cocktail.spit.components.strainer :as strainer]
            [cocktail.spit.routes :as routes]
            [cocktail.stuff.illustration :refer [illustration]]
            [cocktail.stuff.util :as util]
            [re-frame.core :as rf]))

(declare card header title body buttons)

;; NOTE Dispatch and updating of the cocktails are now handled in the top
;;      level component, not sure if this let pattern is a good idea
(defn main []
  (let [cs (util/->transit+json @(rf/subscribe [:strainer]))
        _ (rf/dispatch [:strain-cocktails cs])
        cocktails @(rf/subscribe [:strained-cocktails])]
    [:<>
     [strainer/main]
     [:main.cocktails
      [:section#cocktails.dense-grid 
       (for [cocktail cocktails]
         ^{:key (:id cocktail)} [card cocktail])]]]))

(defn card [{:keys [ingredients] :as cocktail}]
  [:section.card.hover-card
   [illustration cocktail "80px"]
   [:section.card-contents
    [header cocktail]
    [catalouge/ingredient-list ingredients "card-ingredient"]
    [body cocktail]]
   [catalouge/flags cocktail]])

(defn header [cocktail]
  [:div.card-header
   [title cocktail]])

(defn title [{:keys [title id]}]
  [:a {:href (routes/cocktail-url-for id)} title])

(defn body [{:keys [recipe preparation] :as cocktail}]
  [:div.card-body
   [catalouge/recipe recipe]
   [:p preparation]
   [buttons cocktail]])

(defn buttons [cocktail]
  [:<>
   [catalouge/dispatch-btn "menu-cocktails" "M" cocktail] "    "
   (catalouge/dispatch-btn "library-cocktails" "L"  cocktail)])
