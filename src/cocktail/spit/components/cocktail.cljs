(ns cocktail.spit.components.cocktail
  (:require [clojure.string :as str]
            [cocktail.spit.components.catalouge :as catalouge]
            [cocktail.stuff.illustration :as illustration]
            [cocktail.stuff.util :as util]
            [re-frame.core :as rf]))

(declare illustration header body sidebar recipe metadata)

(defn main []
  (let [cocktail @(rf/subscribe [:active-cocktail])]
    [:main>div#cocktail
     [illustration cocktail]
     [header cocktail]
     [body cocktail]]))

(defn illustration [{:keys [ingredients]}]
  (let [h "200px" w "100%"]
    [:svg.illustration {:style {:height h :width w}}
     (for [ingredient ingredients]
       (illustration/header ingredient w h))]))

(defn header [{:keys [ingredients title]}]
  [:section.header
   [:h1 title]
   [catalouge/ingredient-list ingredients "cocktail-ingredient"]])

(defn body [{:keys [story img] :as cocktail}]
  [:section.cocktail-body 
   [:div.content
    [sidebar cocktail]
    [:div (when story (str/trim story))]
    [:img {:src img}]]
   [metadata cocktail]])

(defn sidebar [{:keys [recipe preparation]}]
  [:aside
   [catalouge/recipe recipe]
   [:p preparation]])

(defn metadata [{:keys [date author url]}]
  [:div.metadata
   [:p (subs (str date) 0 15)]
   [:p "posted by " author]
   [:a {:href url} "view original"]])
