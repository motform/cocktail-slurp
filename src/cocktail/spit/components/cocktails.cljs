(ns cocktail.spit.components.cocktails
  (:require [cocktail.spit.components.catalouge :as catalouge]
            [cocktail.spit.components.strainer :as strainer]
            [cocktail.spit.routes :as routes]
            [cocktail.stuff.illustration :as illustration]
            [cocktail.stuff.util :as util]
            [re-frame.core :as rf]))

(declare card illustration header title body buttons)

;; NOTE Dispatch and updating of the cocktails are now handled in the top
;;      level component, not sure if this let pattern is a good idea
(defn main []
  (let [c (util/->transit+json @(rf/subscribe [:strainer]))
        _ (rf/dispatch [:strain-cocktails c])
        cocktails @(rf/subscribe [:strained-cocktails])]
    [:<>
     [strainer/main]
     [:main
      [:div#cocktails.dense-grid 
       (for [cocktail cocktails]
         ^{:key (:id cocktail)} [card cocktail])]]]))

(defn card [{:keys [ingredients] :as cocktail}]
  [:section.card.hover-card
   [illustration ingredients]
   [:section.card-contents
    [header cocktail]
    [catalouge/ingredient-list ingredients "card-ingredient"]
    [body cocktail]]
   [catalouge/flags cocktail]])

(defn illustration [ingredients]
  (let [h "80px" w "100%"]
    [:svg.illustration {:style {:height h :width w}}
     (for [ingredient ingredients]
       (illustration/header ingredient w h))]))

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
