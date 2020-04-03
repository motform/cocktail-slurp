(ns cocktail.spit.components.strainer
  (:require [cocktail.spit.components.catalouge :as catalouge]
            [re-frame.core :as rf]))

;; TODO split the strain input on spaces, as it seems like we get better
;; results when matching on single words

(declare select-ingredient strained-bit clear-strainer btn-strain)

(defn main []
  (let [bits @(rf/subscribe [:strainer-ingredients])]
    [:div#strainer

     [catalouge/text-input-auto
      {:placeholder "Search…" :id "strain-input"
       :sub :strainer-search
       :on-change #(let [val (-> % .-target .-value str)]
                     (rf/dispatch [:strainer-search val]))}]
     [select-ingredient]
     (for [bit bits] ^{:key bit} [strained-bit bit])
     [clear-strainer]]))

(defn select-ingredient []
  (let [ingredients (sort @(rf/subscribe [:meta-ingredients]))]
    [:select {:name "ingredients"
              :on-change #(rf/dispatch [:strainer-conj :ingredients (-> % .-target .-value)])}
     (for [ingredient ingredients]
       ^{:key ingredient :value ingredient}
       [:option ingredient])]))

;; TODO change this into an input button/submit?
;; TODO make they key dynamic or turn into cataloged component
(defn strained-bit [bit]
  [:button.strainer-bit
   {:on-click #(rf/dispatch [:strainer-disj :ingredients bit])}
   bit])

(defn clear-strainer []
  [:input#btn-strainer-clear
   {:type "button" :value "clear strainer"
    :on-click #(rf/dispatch [:strainer-clear])}])
