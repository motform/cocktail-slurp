(ns cocktail.spit.components.cocktail
  (:require [clojure.string :as str]
            [cocktail.spit.components.catalouge :as catalouge]
            [cocktail.stuff.illustration :refer [illustration]]
            [re-frame.core :as rf]))

(defn metadata [{:cocktail/keys [date author url]}]
  [:div.metadata
   [:p (subs (str date) 0 15)]
   [:p "posted by " author]
   [:a {:href url :target "_blank"} "view original"]])

(defn header [{:cocktail/keys [ingredient title]}]
  [:section.header
   [:h1 title]
   [catalouge/ingredient-list ingredient "cocktail-ingredient"]])

(defn sidebar [{:cocktail/keys [recipe preparation]}]
  [:aside
   [catalouge/recipe recipe]
   [:p preparation]])

(defn body [{:cocktail/keys [story img] :as cocktail}]
  [:section.cocktail-body 
   [:div.content
    [sidebar cocktail]
    [:div.story (when story (str/trim story))]
    [:img {:src img}]]
   [metadata cocktail]])

(defn main []
  (let [cocktail @(rf/subscribe [:cocktail/active])]
    [:main>div.cocktail
     [illustration cocktail "200px"]
     [header cocktail]
     [body cocktail]]))
