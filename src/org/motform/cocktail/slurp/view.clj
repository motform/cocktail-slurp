(ns org.motform.cocktail.slurp.view
  (:require [clojure.string :as str]
            [hiccup.page    :as hiccup]
            [hiccup2.core   :as hiccup2]
            [org.motform.cocktail.slurp.db           :as db]
            [org.motform.cocktail.stuff.illustration :as illustration]
            [org.motform.cocktail.stuff.util         :as util]))

(def pagination-step 50)

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
    [:title title]
    [:meta  {:content "width=device-width, initial-scale=1"
             :name    "viewport"
             :charset "utf-8"}]
    [:body
     (hiccup2/html content)]]))

(defn- header-mobile []
  [:header.strainer-mobile
   [:a.nameplate {:href "/"} "CS"]
   [:div#ftoggle.filter-mobile {:id "ftoggle"} "search"]])

(defn- sidebar [{:strs [ingredient kind search favorites]} {:keys [user/view]}]
  (let [selected-ingredients (if (string? ingredient) #{ingredient} (into #{} ingredient))
        selected-kinds       (if (string? kind) #{kind} (into #{} kind))
        favorites?           (boolean favorites)]
    [:aside#strainer.strainer
     [:a.nameplate {:href "/"} "CS"]

     [:form {:action "/cocktails" :method "get"}

      [:section.search
       [:input {:type "text" :name "search" :id "search" :placeholder "Search" :value search}]]

      [:section.category
       [:h4 "Collections"]
       (list [:input.ingredient-check
              {:type    "checkbox"
               :id      "favorites"
               :name    "favorites"
               :value   "true"
               :checked favorites?}]
             [:label.ingredient {:for "favorites"} "favorites"])]

      [:section.category
       [:h4 "Style"]
       (for [kind (sort (db/all :cocktail/kind))]
         (list [:input.ingredient-check
                {:type    "checkbox"
                 :id      kind
                 :name    "kind"
                 :value   kind
                 :checked (selected-kinds kind)}]
               [:label.ingredient {:for kind} kind]))]

      (for [[category ingredients] util/ingredients]
        [:section.category.ingredients
         [:h4 (name category)]
         (for [ingredient ingredients]
           (list [:input.ingredient-check
                  {:type    "checkbox"
                   :id      ingredient
                   :name    "ingredient"
                   :value   ingredient
                   :checked (selected-ingredients ingredient)}]
                 [:label.ingredient {:for ingredient} ingredient [:span.possible-cocktails-count ""]]))])

      [:input {:type "submit" :value "STRAIN"}]]

     [:section.category.settings 
      [:h4 "Settings"]
      [:form {:action "/set-cookie" :method "post"}
       [:input.ingredient-check
        {:type    "checkbox"
         :id      "view"
         :name    "view"
         :checked true
         :value   (case view "expanded" "normal" "normal" "expanded" "expanded")}]
       [:input.setting {:type "submit" 
                        :value (str "Expanded cards are " (if (= view "expanded") "on" "off") )}]]]]))

(defn- card-recipe [recipe & {:keys [expanded?]}]
  (let [ingredients (str/split-lines recipe)]
    [:section.card-recipe {:class (when expanded? "card-recipe-expanded")}
     (for [ingredient ingredients]
       (let [{:keys [measurement name]} (util/split-ingredient ingredient)]
         [:span.card-recipe-row 
          [:span.card-recipe-measurement measurement] 
          [:span.card-recipe-ingredient name]]))]))

(defmulti cocktail-card :card/type)

(defmethod cocktail-card "expanded" 
  [{:cocktail/keys [title id preparation recipe img]
    :user/keys [favorite] 
    :as cocktail}]
  [:section.card
   (illustration/illustration cocktail "60px")
   [:div.card-title-container.card-title-container-expanded
    [:a.card-title {:href (str "/cocktail/" id)} title]
    [:p.card-title-favorite (when favorite "!")]]
   [:div.card-body.expanded
    [:div.card-body-expanded-content
     (card-recipe recipe :expanded? true)
     [:p.card-preparation preparation]]
    [:div.card-img [:img {:src img}]]]])

(defmethod cocktail-card "normal" 
  [{:cocktail/keys [title id preparation recipe] 
    :user/keys [favorite] 
    :as cocktail}]
  [:section.card
   (illustration/illustration cocktail "60px")
   [:div.card-body
    [:div.card-title-container
     [:a.card-title {:href (str "/cocktail/" id)} title]
     [:p.card-title-favorite (when favorite "!")]]
    (card-recipe recipe)
    [:p.card-preparation preparation]]])

(defn- pagination-query-string [query-string]
  (str "cocktails?" (str/replace query-string #"&cursor=\d+$" "") "&"))

(defn- cocktail-cards [strainer {:keys [pagination/cursor pagination/origin pagination/query-string user/cookies]}]
  (let [{:keys [cursor cocktails end?]} (db/paginate cursor pagination-step db/strain strainer)
        card-view                       (or (get-in cookies ["view" :value]) "expanded")]
    (if (empty? cocktails)
      [:div#cards.container
       [:div.empty (util/empty-quip)]]
      [:div#cards.container
       [:section.cards {:class (when (= card-view "expanded") "cards-expanded")}
        (for [cocktail cocktails]
          (cocktail-card (assoc cocktail :card/type card-view)))]
       [:footer
        [:a.paginate {:href  (str (if (= origin :home) "?" (pagination-query-string query-string)) "cursor=" (max 0 (- cursor (* 2 pagination-step))))
                      :class (when (>= 0 (- cursor pagination-step)) "hide")}
         "←"]
        [:p.tagline "quality versus quantity does not have to be a winner-take-all proposition"]
        [:a.paginate {:href  (str (if (= origin :home) "?" (pagination-query-string query-string)) "cursor=" cursor)
                      :class (when end? "hide")}
         "→"]]])))

(defn- cocktail-page [{:cocktail/keys [id recipe title date preparation story url img] :as cocktail}]
  (list [:main.cocktail-page
         (illustration/illustration cocktail "200px")
         [:section.cocktail 
          [:section.cocktail-header
           [:h1.page-title title]
           [:form {:action "/favorite" :method "post"}
            [:input.ingredient-check {:type "checkbox" :name "id" :value id :checked true}]
            [:input.favorite {:type "submit" 
                              :value (if (:user/favorite cocktail) "!!!" "!")}]]]
          [:section.cocktail-body
           [:div.page-content
            [:aside.page-preparation
             [:section.page-recipe
              (for [ingredient (str/split-lines recipe)]
                (let [{:keys [measurement name]} (util/split-ingredient ingredient)]
                  [:span.page-recipe-row 
                   [:span.page-recipe-measurement measurement] 
                   [:span.page-recipe-ingredient name]]))]
             [:p preparation]]
            [:div.page-story (when story (str/trim story))]
            (when img
              [:div.page-img
               [:img {:src img}]])]
           [:div.page-metadata
            [:p (str (subs (str date) 0 11) (subs (str date) 24 28))]
            [:a {:href url :target "_blank"} "view original"]
            [:a {:href (str "/spill/" id)} "Spill"]]]]]
        
        [:footer
         [:a.nameplate {:href "/"} "CS"]]))

;;; PAGES

(defn cocktails [{{:strs [cursor] :as strainer} :query-params 
                  :keys [query-string cookies]} 
                 origin]
  (page "Cocktail Slurp"
    [:main.cocktails
     (header-mobile)
     (sidebar strainer {:user/view (get-in cookies ["view" :value])})
     (cocktail-cards strainer {:pagination/cursor       (if cursor (Integer. cursor) 0)
                               :pagination/origin       origin
                               :pagination/query-string query-string
                               :user/cookies            cookies})
     (hiccup/include-js "/js/script.js")]))

(defn cocktail [id]
  (let [cocktail (db/cocktail-by-id id)]
    (page (str "Cocktail Slurp | " (str/capitalize (:cocktail/title cocktail)))
      (cocktail-page cocktail))))

(comment
  
  )
