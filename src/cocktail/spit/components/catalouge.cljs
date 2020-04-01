(ns cocktail.spit.components.catalouge
  (:require [clojure.string :as str]
            [cocktail.stuff.util :as util]
            [re-frame.core :as rf]
            [reagent.core :as r]))

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
   {:on-click #(rf/dispatch [:strainer-conj :ingredients ingredient])}
   ingredient])

(defn ingredient-list [ingredients class]
  [:ul.ingredients {:class class} 
   (sort (for [ingredient ingredients]
           ^{:key (util/gen-key ingredient ingredients)}
           ;; WARN might result in a key collision when we have two drinks with identical ingredients
           [ingredient' ingredient]))])

(defn flags [cocktail]
  (let [menu @(rf/subscribe [:menu-cocktails])
        library @(rf/subscribe [:library-cocktails])]
    [:div.flags
     (when (menu cocktail) [:div.flag-menu.flag])
     (when (library cocktail) [:div.flag-library.flag])]))

(defn text-input [{:keys [title on-save]}]
  (let [val (r/atom title)
        stop #(reset! val "")
        save #(let [v (-> @val str str/trim)]
                (on-save v)
                (stop))]
    (fn [props]
      [:input
       (merge (dissoc props :on-save :title)
              {:type "text" :value @val :on-blur save :autoFocus true
               :on-change #(reset! val (-> % .-target .-value))
               :on-key-down #(case (.-which %)
                               13 (save)
                               27 (stop)
                               nil)})])))
