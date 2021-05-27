(ns cocktail.spit.components.cocktails
  (:require [cocktail.spit.components.catalouge :as catalouge]
            [cocktail.spit.components.strainer :as strainer]
            [cocktail.stuff.illustration :refer [illustration]]
            [re-frame.core :as rf]
            [reitit.frontend.easy :refer [href]]))

(defn toggle [label cocktail collection coll-k]
  (let [in? (collection cocktail)
        event (keyword :collection (if-not in? :conj :disj))]
    [:button.toggle.collection.collection-inactive
     {:on-click #(rf/dispatch [event coll-k cocktail])
      :class (when in? "in-collection")}
     label]))

(defn buttons [cocktail [library menu]]
  [:div.collections
   [toggle "Menu" cocktail menu :menu]
   [toggle "Library" cocktail library :library]])

(defn card [{:cocktail/keys [recipe preparation ingredient title id] :as cocktail} collections]
  [:section.card
   [illustration cocktail "60px"]
   [:div.card-body
    [:a.title
     {:href (href :route/cocktail {:id id}) :target "_blank"}
     title]
    [catalouge/ingredient-list ingredient "card-ingredient"]
    [catalouge/recipe recipe]
    [:p preparation]]
   [buttons cocktail collections]])

(defn main []
  (let [strainer @(rf/subscribe [:strainer/keys [:kind :ingredient :search]])
        cocktails @(rf/subscribe [:cocktails/strained])
        cursor @(rf/subscribe [:cocktails/cursor])

        ;; NOTE We get the collections at top level to save local sub-calls
        menu @(rf/subscribe [:collection/cocktails :menu])   
        library @(rf/subscribe [:collection/cocktails :library])]
    [:main.cocktails
     [strainer/sidebar]
     [:div>section.cards
      (for [cocktail cocktails]
        ^{:key (:cocktail/id cocktail)} [card cocktail [library menu]])
      (when cursor 
        [:div>input.next-page
         {:type "button"
          :value "But wait, there is more!"
          :on-click #(rf/dispatch [:strainer/next-page strainer cursor])}])]]))
