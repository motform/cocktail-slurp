(ns cocktail-spit.components.catalouge
  (:require [re-frame.core :as rf]
            [cocktail-spit.helpers :as helpers]))

(defn dispatch-btn [k label cocktail]
  (let [collection @(rf/subscribe [(keyword k)])
        in? (collection cocktail)
        op (if-not in? "+ " "– ")
        f (if-not in? "-conj" "-disj")
        e (keyword (str k f))]
    [:button.dispatch-btn {:class k :on-click #(rf/dispatch [e cocktail])}
     op label]))

(defn message-empty [section]
  [:div.empty
   (str "your " section " is empty, add some slurps!")])

(defn ingredient' [ingredient]
  [:li.ingredient.clickable-ingredient
   {:on-click #(rf/dispatch [:strainer-ingredient-conj ingredient])}
   ingredient])

(defn ingredient-list [ingredients class]
  [:ul.ingredients {:class class} 
   (sort (for [ingredient ingredients]
           ^{:key (helpers/gen-key ingredient ingredients)}
           ;; WARN might result in a key collision when we have two drinks with identical ingredients
           [ingredient' ingredient]))])

(defn flags [cocktail]
  (let [menu @(rf/subscribe [:menu-cocktails])
        library @(rf/subscribe [:library-cocktails])]
    [:div.flags
     (when (menu cocktail) [:div.flag-menu.flag])
     (when (library cocktail) [:div.flag-library.flag])]))
