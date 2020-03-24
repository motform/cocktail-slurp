(ns cocktail.spit.components.library
  (:require [re-frame.core :as rf]
            [cocktail.spit.routes :as routes]
            [cocktail.spit.components.catalouge :as catalouge]))

;; TODO other sorting options beyond initial

(declare clear-library empty-library cocktails-by-name section
         sections initial section-cocktails section-cocktail)

(defn main []
  (let [library @(rf/subscribe [:library-cocktails])]
    [:<>
     [sections (cocktails-by-name library)]
     [clear-library]]))

(defn sections [cocktails]
  [:main#library.dense-grid
   (if-not (empty? cocktails)
     (for [[letter cocktails'] cocktails]
       ^{:key letter} [section letter cocktails'])
     [catalouge/message-empty "library"])])

(defn section [letter cocktails]
  [:div.section
   [initial letter]
   [section-cocktails cocktails]])

(defn section-cocktails [cocktails]
  [:div.section-cocktails
   (for [cocktail cocktails]
     ^{:key (:id cocktail)} [section-cocktail cocktail])])

(defn initial [letter]
  [:h2.section-initial letter]) 

(defn section-cocktail [{:keys [title ingredients id]}]
  [:div.section-cocktail
   [:a {:href (routes/cocktail-url-for id)} title]
   [:ul.section-ingredients
    #_(for [ingredient ingredients]
       ^{:key (helpers/gen-key ingredient id)}
       [:li.section-ingredient ingredient])]])

(defn clear-library []
  ;; TODO implement this with re-frame-undo
  [:div {:style {:width "100%"}}
   [:button#btn-library-clear
    {:on-click
     #(when (js/confirm "Are you sure you want to clear the library?")
        (rf/dispatch [:library-clear]))}
    "clear library"]])

(defn cocktails-by-name [cocktails]
  (->> cocktails
      (group-by #(first (:title %)))
      (into (sorted-map))))
