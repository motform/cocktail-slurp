(ns org.motform.cocktail.slurp.view
  (:require [clojure.string :as str]
            [hiccup.page    :as hiccup]
            [org.motform.cocktail.slurp.db           :as db]
            [org.motform.cocktail.stuff.illustration :as illustration]
            [org.motform.cocktail.stuff.util         :as util]))

;;; COMPONENTS 

(defn- page
  "Basic page template."
  {:style/indent 1}
  [title & content]
  (hiccup/html5
   {:lang "en"}
   [:head
    (hiccup/include-css "css/style.css")
    (hiccup/include-css "css/fonts.css")
    [:title (str "Cocktail Slurp | " title)]
    [:meta  {:content "text/html;" :charset "utf-8"}]
    [:body
     content]]))

(defn- sidebar []
  (let [kinds       (sort (db/all :cocktail/kind))]
    [:aside.strainer
     [:form {:action "/cocktails" :method "get"}
      [:section.search
       [:input {:type "text" :name "search" :id "search" :placeholder "Search"}]]
      [:section.kinds
       [:h4 "Style"]
       (for [kind kinds]
         [:div.kind
          [:input {:type "radio" :id kind :name "kind" :value kind}]
          [:label {:for kind} kind]])]
      (for [[category ingredients] util/ingredients]
        [:section.category
         [:h4 (name category)]
         (for [ingredient ingredients]
           [:div.ingredient 
            [:input {:type "checkbox" :id ingredient :name "ingredient" :value ingredient}]
            [:label {:for ingredient} ingredient]])])
      [:section.submit
       [:input {:type "submit" :value "Strain"}]]]]))

(defn recipe [recipe]
  (let [ingredients (str/split-lines recipe)]
    [:table>tbody
     (for [ingredient ingredients]
       (let [{:keys [measurement name]} (util/split-ingredient ingredient)]
         ^{:key (str measurement name)}
         [:tr
          ;; NOTE Some recipes are very annoying as they have irregular
          ;; formats for the measurements, causing them to parse as
          ;; empty strings - a valid React key but a problem if we
          ;; have two or more. For these cases, we generate a random
          ;; that most likely won't collide. The fact that this is not
          ;; deterministic should not be a problem as these
          ;; components, as of this comment, never re-render.
          ^{:key (if-not (str/blank? measurement)
                   measurement
                   (* (rand-int 999) (rand-int 999)))}
          [:td measurement] 
          ^{:key name} [:td name]]))]))

(defn cocktail-card [{:cocktail/keys [title id ingredient preparation recipe]  :as cocktail}]
  [:section.card
   (illustration/illustration cocktail "60px")
   [:div.card-body
    [:a.title {:href ""} title]
    [:ul.ingredients {:class "card-ingredient"} 
     (sort (for [i ingredient] [:li.ingredient i]))]

    ]
   ])

(defn cocktail-cards
  "Call with zero arity for the latest cocktails."
  ([] (cocktail-cards {}))
  ([strainer]
   (let [{:keys [cursor cocktails]} (db/paginate 0 20 db/strain strainer)]
     [:section.cards
      (for [cocktail cocktails]
        (cocktail-card cocktail))])))

;;; PAGES

(defn home [_]
  (page "Home" 
    [:main.cocktails
     (sidebar)
     (cocktail-cards)]))

(defn cocktails [{:strs [kind ingredient search]}]
  (page (str (or kind "Buncha") "Cocktails") 
    [:main.cocktails
     (sidebar)
     "huh"]))

(comment
  )
