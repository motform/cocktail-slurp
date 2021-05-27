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
    (hiccup/include-css "css/reset.css")
    (hiccup/include-css "css/fonts.css")
    (hiccup/include-css "css/style.css")
    [:title (str "Cocktail Slurp | " title)]
    [:meta  {:content "text/html;" :charset "utf-8"}]
    [:body
     content]]))

(defn- sidebar []
  [:aside.strainer
   [:form {:action "/cocktails" :method "get"}

    [:section.search
     [:input {:type "text" :name "search" :id "search" :placeholder "Search"}]]

    [:section.kinds
     [:h4 "Style"]
     (for [kind (sort (db/all :cocktail/kind))]
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
     [:input {:type "submit" :value "Strain"}]]]])

(defn card-recipe [recipe]
  (let [ingredients (str/split-lines recipe)]
    [:section.card-recipe
     (for [ingredient ingredients]
       (let [{:keys [measurement name]} (util/split-ingredient ingredient)]
         [:span.card-recipe-row 
          [:span.card-recipe-measurement measurement] 
          [:span.card-recipe-ingredient name]]))]))

(defn cocktail-cards
  "Call with zero arity for the latest cocktails."
  ([] (cocktail-cards {}))
  ([strainer]
   (let [{:keys [cursor cocktails]} (db/paginate 0 20 db/strain strainer)]
     [:section.cards
      (for [{:cocktail/keys [title id preparation recipe] :as c} cocktails]
        [:section.card
         (illustration/illustration c "60px")
         [:div.card-body
          [:a.card-title {:href id} title]
          (card-recipe recipe)
          [:p preparation]]])])))

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
