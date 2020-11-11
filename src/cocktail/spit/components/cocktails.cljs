(ns cocktail.spit.components.cocktails
  (:require [cocktail.spit.components.catalouge :as catalouge]
            [cocktail.spit.components.strainer :as strainer]
            [cocktail.spit.events :as event]
            [cocktail.stuff.illustration :refer [illustration]]
            [re-frame.core :as rf]
            [reitit.frontend.easy :refer [href]]))

;; (defn buttons [cocktail]
;;   [:<>
;;    [catalouge/dispatch-btn "menu-cocktails" "M" cocktail] "    "
;;    (catalouge/dispatch-btn "library-cocktails" "L"  cocktail)])

(defn card [{:keys [recipe preparation ingredients title id] :as cocktail}]
  [:section.card
   [illustration cocktail "60px"]
   [:div.card-body
    [:a.title {:href (href :route/cocktail {:id id})} title]
    [catalouge/ingredient-list ingredients "card-ingredient"]
    [catalouge/recipe recipe]
    [:p preparation]
    #_[buttons cocktail]
    #_[catalouge/flags cocktail]]])

;; NOTE Dispatch and updating of the cocktails are now handled in the top
;;      level component, not sure if this let pattern is a good idea
(defn main []
  (let [cs @(rf/subscribe [:strainer/keys [:kind :collection :ingredients :search]])
        _ (rf/dispatch [:strainer/request-cocktails cs])
        cocktails (:cocktails @(rf/subscribe [:strainer/keys [:cocktails]]))] ;; NOTE this line looks a bit off
    [:main.cocktails
     [strainer/sidebar]
     [:section.cards
      (for [cocktail cocktails]
        ^{:key (:id cocktail)} [card cocktail])]]))
