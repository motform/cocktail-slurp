(ns cocktail.spit.components.cocktails
  (:require [cocktail.spit.components.catalouge :as catalouge]
            [cocktail.spit.components.strainer :as strainer]
            [cocktail.spit.events :as event]
            [cocktail.stuff.illustration :refer [illustration]]
            [re-frame.core :as rf]
            [reitit.frontend.easy :refer [href]]))

(defn dispatch-btn [label cocktail collection coll-k]
  (let [in? (collection cocktail)
        op (if-not in? "+" "–")
        event (keyword :collection (if-not in? :conj :disj))]
    [:button.dispatch-btn
     {:on-click #(rf/dispatch [event coll-k cocktail])}
     op label]))

(defn buttons [cocktail [library menu]]
  [:<>
   [dispatch-btn "M" cocktail menu :menu]
   [dispatch-btn "L" cocktail library :library]])

(defn card [{:keys [recipe preparation ingredients title id] :as cocktail} collections]
  [:section.card
   [illustration cocktail "60px"]
   [:div.card-body
    [:a.title {:href (href :route/cocktail {:id id})} title]
    [catalouge/ingredient-list ingredients "card-ingredient"]
    [catalouge/recipe recipe]
    [:p preparation]
    [buttons cocktail collections]]])

(defn main []
  (let [strainer @(rf/subscribe [:strainer/keys [:kind :collection :ingredients :search]])
        _ (rf/dispatch [:strainer/request-cocktails strainer]) ;; TODO move to route-controller
        cocktails @(rf/subscribe [:strainer/cocktails])

        ;; NOTE We get the collections at top level to save local sub-calls
        menu @(rf/subscribe [:collection/cocktails :menu])   
        library @(rf/subscribe [:collection/cocktails :library])]
    [:main.cocktails
     [strainer/sidebar]
     [:section.cards
      (for [cocktail cocktails]
        ^{:key (:id cocktail)} [card cocktail [library menu]])]]))
