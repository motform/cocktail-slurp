(ns cocktail.spit.components.strainer
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :as rf]))

;; TODO split the strain input on spaces, as it seems like we get better
;; results when matching on single words

(defn clear-strainer []
  [:button.toggle.clear
   {:on-click #(rf/dispatch [:strainer/clear])}
   "clear strainer"])

(defn strainer-toggle [item strainer k]
  [:button.toggle
   {:class (when (contains? strainer item) "toggle-active")
    :on-click #(rf/dispatch [:strainer/toggle k item])}
   item])

(defn toggles [k list strainer]
  [:div.toggles
   (for [item (sort list)]
     ^{:key item} [strainer-toggle item (k strainer) k])])

(defn toggle [k list strainer]
  [:section.strainer-category
   [:p (name k)]
   [toggles k list strainer]])

(defn toggle-ingredients []
  (let [*filter (r/atom "")]
    (fn [list strainer]
      (let [ingredients (filter #(str/includes? % @*filter)
                                (set/difference list (:ingredient strainer)))]
        [:section
         [:p "ingredients"]
         [:input {:type "text" :placeholder "Filter…"
                  :value @*filter 
                  :on-change #(reset! *filter (-> % .-target .-value))}]
         [toggles :ingredient (:ingredient strainer) strainer]
         [toggles :ingredient ingredients strainer]]))))

(defn search [{:keys [search]}]
  (let [*state (r/atom {:timer 0 :search search})]
    (fn [] 
      (js/setTimeout #(swap! *state update :timer inc) 1000)
      (when (= (:timer @*state) 3)
        (rf/dispatch [:strainer/search (:search @*state)]))
      [:input
       {:type "text" :placeholder "Search…"
        :value (:search @*state)
        :on-change #(swap! *state assoc :timer 0 :search (.. % -target -value))}])))

(defn sidebar []
  (let [strainer @(rf/subscribe [:strainer/all])
        ingredients @(rf/subscribe [:meta/ingredent])]
    [:aside.strainer
     [search strainer]
     [toggle :collection #{:library :menu} strainer]
     [toggle :kind #{"stirred" "shaken" "punch"} strainer] ;; TODO make into a radio button
     [toggle-ingredients ingredients strainer]
     [clear-strainer]]))
