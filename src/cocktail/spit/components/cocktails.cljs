(ns cocktail.spit.components.cocktails
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [cocktail.spit.routes :as routes]
            [cocktail.spit.components.catalouge :as catalouge]
            [cocktail.spit.components.strainer :as strainer]))

(declare card header title body buttons)

(defn main [cocktails]
  [:<>
   [strainer/main]
   [:main
    [:div#cocktails.dense-grid 
     (for [cocktail cocktails]
       ^{:key (:id cocktail)} [card cocktail])]]])

(defn card [{:keys [ingredients] :as cocktail}]
  [:div.card.hover-card.line-left
   [header cocktail]
   [catalouge/ingredient-list ingredients "card-ingredient"]
   [body cocktail]])

(defn header [cocktail]
  [:div.card-header
   [title cocktail]
   [catalouge/flags cocktail]])

(defn title [{:keys [title id]}]
  [:a {:href (routes/cocktail-url-for id)} title])

(defn body [{:keys [recipe preparation] :as cocktail}]
  [:div.card-body
   [:p recipe] [:br]
   [:p preparation] [:br] [:br]
   [buttons cocktail]])

(defn buttons [cocktail]
  [:<>
   [catalouge/dispatch-btn "menu-cocktails" "M" cocktail] "    "
   (catalouge/dispatch-btn "library-cocktails" "L"  cocktail)])
