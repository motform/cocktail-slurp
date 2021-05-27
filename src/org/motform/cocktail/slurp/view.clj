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
    (hiccup/include-css "/css/reset.css")
    (hiccup/include-css "/css/fonts.css")
    (hiccup/include-css "/css/style.css")
    [:title (str "Cocktail Slurp | " title)]
    [:meta  {:content "text/html;" :charset "utf-8"}]
    [:body
     content]]))

(defn- sidebar []
  [:aside.strainer
   [:p.nameplate "cocktail slurp"]
   [:form {:action "/cocktails" :method "get"}

    [:section.search
     [:input {:type "text" :name "search" :id "search" :placeholder "Search"}]]

    [:section.kinds
     [:h4 "Style"]
     (for [kind (sort (db/all :cocktail/kind))]
       [:div.ingredient
        [:input.ii {:type "checkbox" :id kind :name "kind" :value kind}]
        [:label.il {:for kind} kind]])]

    (for [[category ingredients] util/ingredients]
      [:section.category
       [:h4 (name category)]
       (for [ingredient ingredients]
         [:div.ingredient 
          [:input.ii {:type "checkbox" :id ingredient :name "ingredient" :value ingredient}]
          [:label.il {:for ingredient} ingredient]])])

    [:input {:type "submit" :value "Strain"}]]])

(defn- card-recipe [recipe]
  (let [ingredients (str/split-lines recipe)]
    [:section.card-recipe
     (for [ingredient ingredients]
       (let [{:keys [measurement name]} (util/split-ingredient ingredient)]
         [:span.card-recipe-row 
          [:span.card-recipe-measurement measurement] 
          [:span.card-recipe-ingredient name]]))]))

(defn- cocktail-cards
  "Call with zero arity for the latest cocktails."
  ([] (cocktail-cards {}))
  ([strainer]
   (let [{:keys [cursor cocktails]} (db/paginate 0 20 db/strain strainer)]
     [:section.cards
      (for [{:cocktail/keys [title id preparation recipe] :as c} cocktails]
        [:section.card
         (illustration/illustration c "60px")
         [:div.card-body
          [:a.card-title {:href (str "/cocktail/" id)} title]
          (card-recipe recipe)
          [:p preparation]]])])))

(defn- cocktail-page [{:cocktail/keys [ingredient title date author preparation story url img] :as c}]
  [:section.cocktail
   (illustration/illustration c "200px")
   [:section.cocktail-header
    [:h1 title]]
   [:section.cocktail-body
    [:div.content
     [:aside
      [:section.card-recipe
       (for [i ingredient]
         (let [{:keys [measurement name]} (util/split-ingredient i)]
           [:span.card-recipe-row 
            [:span.card-recipe-measurement measurement] 
            [:span.card-recipe-ingredient name]]))]
      [:p preparation]]
     [:div.story (when story (str/trim story))]
     [:img {:src img}]]
    [:div.metadata
     [:p (subs (str date) 0 15)]
     [:p "posted by " author]
     [:a {:href url :target "_blank"} "view original"]]]])

;;; PAGES

(defn home [_]
  (page "Home" 
    [:main.cocktails
     (sidebar)
     (cocktail-cards)]))

(defn cocktail [id]
  (let [cocktail (db/cocktail-by-id id)]
    (page "(:cocktail/title cocktail)"
      [:main.cocktail
       (cocktail-page cocktail)])))

(defn cocktails [{:strs [kind ingredient search]}]
  (page (str (or kind "Buncha") "Cocktails") 
    [:main.cocktails
     (sidebar)
     "huh"]))

(comment
  )
