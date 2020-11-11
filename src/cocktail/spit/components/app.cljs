(ns cocktail.spit.components.app
  (:require [re-frame.core :as rf]
            [cocktail.spit.components.cocktails :as cocktails]
            [cocktail.spit.components.cocktail :as cocktail]))

(def active-page
  {:cocktails [cocktails/main]
   :cocktail [cocktail/main]})

(defn main []
  (let [page @(rf/subscribe [:page/active])]
    [:<>
     [active-page page]
     [:footer "quality versus quantity does not have to be a winner-take-all proposition"]]))
