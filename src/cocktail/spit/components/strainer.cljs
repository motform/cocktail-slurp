(ns cocktail.spit.components.strainer
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [cognitect.transit :as t]
            [clojure.string :as str]))

(declare strain-input-freeform strained-bit clear-strainer test-ajax)

(defn main []
  (let [bits @(rf/subscribe [:strainer-ingredients])]
    [:div#strainer
     [strain-input-freeform
      {:placeholder "Strain…" :id "strain-input"
       :on-save #(when (seq %) (rf/dispatch [:strainer-ingredient-conj %]))}]
     (for [bit bits] ^{:key bit} [strained-bit bit])
     [test-ajax]
     [clear-strainer]]))

(defn strain-input-freeform [{:keys [title on-save]}]
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

;; TODO change this into an input button/submit?
(defn strained-bit [bit]
  [:button.strainer-bit
   {:on-click #(rf/dispatch [:strainer-ingredient-disj bit])}
   bit])

;; (defn test-ajax []
;;   [:button {:on-click #(rf/dispatch
;;                         [:strain-cocktails (t/write (t/writer :json) {:cocktail "old ironsides"})])}
;;    "ajax!"])

(defn test-ajax []
  [:button {:on-click #(rf/dispatch
                        [:cocktail-feed 10])}
   "ajax!"])

(defn clear-strainer []
  [:input#btn-strainer-clear
   {:type "button" :value "clear strainer"
    :on-click #(rf/dispatch [:strainer-clear])}])
