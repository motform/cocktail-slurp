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

(defn toggle-ingredients [list strainer]
  (let [*filter (r/atom "")]
    (fn [list strainer]
      (let [ingredients (filter #(str/includes? % @*filter)
                                (set/difference list (:ingredients strainer)))]
        [:section
         [:p "ingredients"]
         [:input {:type "text" :placeholder "Filter…"
                  :value @*filter 
                  :on-change #(reset! *filter (-> % .-target .-value))}]
         [toggles :ingredients (:ingredients strainer) strainer]
         [toggles :ingredients ingredients strainer]]))))

(defn search [{:keys [search]}]
  [:input
   {:type "text" :placeholder "Search…"
    :value search :autoFocus true
    :on-change #(let [val (-> % .-target .-value str)]
                  (rf/dispatch [:strainer/search val]))}])

(defn sidebar []
  (let [strainer @(rf/subscribe [:strainer/keys [:kind :collection :ingredients :search]])
        ingredients (:ingredients @(rf/subscribe [:meta/keys [:ingredients]]))]
    [:aside.strainer
     [search strainer]
     [toggle :collection #{"library" "menu"} strainer]
     [toggle :kind #{"stirred" "shaken" "punch"} strainer]
     [toggle-ingredients ingredients strainer]
     [clear-strainer]]))
