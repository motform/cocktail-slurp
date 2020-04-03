(ns cocktail.spit.components.app
  (:require [re-frame.core :as rf]
            [cocktail.spit.components.header :as header]
            [cocktail.spit.components.about :as about]
            [cocktail.spit.components.library :as library]
            [cocktail.spit.components.cocktails :as cocktails]
            [cocktail.spit.components.cocktail :as cocktail]
            [cocktail.spit.components.menu :as menu]))

(defn active-page [page]
  (case page
    :cocktails [cocktails/main]
    :cocktail [cocktail/main]
    :library [library/main]
    :about [about/main]
    :menu [menu/main]))

(defn main []
  (let [page @(rf/subscribe [:active-page])]
    [:<>
     [header/main]
     [active-page page]
     [:footer "quality versus quantity does not have to be a winner-take-all proposition"]]))
