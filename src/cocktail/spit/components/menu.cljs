(ns cocktail.spit.components.menu
  (:require [re-frame.core :as rf]
            [cocktail.spit.components.cocktails :as cocktails]
            [cocktail.spit.components.catalouge :as catalouge]))

;; TODO menu should be a mix between cocktail and cocktails
;;      naming is hard

(defn main []
  (let [cocktails @(rf/subscribe [:menu-cocktails])]
    [:main#menu.dense-grid
     (if-not (empty? cocktails)
       (for [cocktail cocktails]
         ^{:key (:id cocktail)} [cocktails/card cocktail])
       [catalouge/message-empty "menu"])]))
