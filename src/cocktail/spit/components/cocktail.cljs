(ns cocktail.spit.components.cocktail
  (:require [re-frame.core :as rf]
            [cocktail.spit.components.catalouge :as catalouge]))

(declare metadata header byline body)

(defn main []
  (let [cocktail @(rf/subscribe [:active-cocktail])
        _ (rf/dispatch [:cocktail-title (:title cocktail)])]
    [:main#cocktail.dense-grid
     [metadata cocktail]
     [body cocktail]
     [:img {:align :right :src "https://4.bp.blogspot.com/-VmEFTEAeqCo/XfkY_FnaWiI/AAAAAAAAMoc/aE-IrGPz5cwKdHK5eBxmLMkJV-I6_NknACLcBGAsYHQ/s320/dickensian5079.jpg"}]]))

;; TODO refactor
(defn metadata [{:keys [ingredients] :as cocktail}]
  [:div#metadata
   [header cocktail]
   [catalouge/ingredient-list ingredients "cocktail-ingredient"]
   [:p {:style {:margin-top "15rem"}} (:recipe cocktail)]
   [:p {:style {:margin-top "5rem"}}  (:preparation cocktail)]
   [:div {:style {:height "50rem"}}]
   [byline cocktail]])

(defn header [cocktail]
  [:div.card-header
   [:h1 (:title cocktail)]
   [catalouge/flags cocktail]])

(defn byline [{:keys [date author url]}]
  [:div.cockail-footer
   [:p #_date
    [:br] "by " author
    [:br] [:a {:href url} "view original"]]])

(defn body [cocktail]
  [:div.metadata
   [:p.cocktail-body (:story cocktail)]])
