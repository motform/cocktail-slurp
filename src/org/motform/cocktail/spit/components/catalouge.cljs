(ns cocktail.spit.components.catalouge
  (:require [clojure.string :as str]
            [cocktail.stuff.util :as util]
            [re-frame.core :as rf]))

(defn ingredient' [ingredient]
  [:li.ingredient.clickable-ingredient
   {:on-click #(rf/dispatch [:strainer/conj :ingredient ingredient])}
   ingredient])

(defn ingredient-list [ingredients class]
  [:ul.ingredients {:class class} 
   (sort (for [ingredient ingredients]
           ^{:key (util/gen-key ingredient ingredients)}
           ;; WARN might result in a key collision when we have two drinks with identical ingredients
           [ingredient' ingredient]))])

;; (defn flags [cocktail]
;;   (let [menu @(rf/subscribe [:menu-cocktails])
;;         library @(rf/subscribe [:library-cocktails])]
;;     [:div.flags
;;      (when (menu cocktail) [:div.flag-menu.flag])
;;      (when (library cocktail) [:div.flag-library.flag])]))

(defn recipe [recipe]
  (let [ingredients (str/split-lines recipe)]
    [:table>tbody
     (for [ingredient ingredients]
       (let [{:keys [measurement name]} (util/split-ingredient ingredient)]
         ^{:key (str measurement name)}
         [:tr
          ;; NOTE Some recipes are very annoying as they have irregular
          ;; formats for the measurements, causing them to parse as
          ;; empty strings - a valid React key but a problem if we
          ;; have two or more. For these cases, we generate a random
          ;; that most likely won't collide. The fact that this is not
          ;; deterministic should not be a problem as these
          ;; components, as of this comment, never re-render.
          ^{:key (if-not (str/blank? measurement)
                   measurement
                   (* (rand-int 999) (rand-int 999)))}
          [:td measurement] 
          ^{:key name} [:td name]]))]))
