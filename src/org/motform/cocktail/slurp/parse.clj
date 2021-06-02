(ns org.motform.cocktail.slurp.parse
  (:require [clojure.instant :as instant]
            [clojure.set :as set]
            [clojure.string :as str]
            [hickory.select :as select]
            [org.motform.cocktail.stuff.util :as util]))

(defn- id [post]
  (assoc post :cocktail/id
         (-> (select/select (select/child (select/attr :name)) post)
             first :attrs :name)))

(defn- url [post]
  (assoc post :cocktail/url
         (-> (select/select (select/child (select/class :timestamp-link)) post)
             first :attrs :href)))

(defn- title [post]
  (assoc post :cocktail/title
         (-> (select/select (select/child (select/class :post-title)) post)
             first :content second :content first)))

(defn- author [post]
  (assoc post :cocktail/author
         (-> (select/select (select/child (select/class :post-author)) post)
             first :content second :content first)))

;; TODO make this an instant instead of a string
(defn- date [post]
  (let [date (-> (select/select (select/child (select/class :timestamp-link)) post)
                 first :content first :attrs :title)]
    (assoc post :cocktail/date (instant/read-instant-date date))))

(defn- flatten-anchors
  "Takes a vec of text nodes Hickory has extracted from a <p> and
  flattens the anchors into their `:content`. Nodes have are pairs
  str {meta} in a vec, with :content holding the anchor str in
  cases of that tag, which we would otherwise miss by just
  filtering for the strings. Does not preserve URLs."
  [x]
  (cond 
    (string? x) (str/trim x)
    (get x :content) "\n\n"
    (map? x)    (or (-> x :content first) "\n")
    :else (throw (Exception. "invalid input to `flatten-anchors`"))))

(defn- body->str [body]
  (->> body (map flatten-anchors) (filter string?) (apply str)))

(defn- split-body [body]
  (map #(str/replace % #"  " "\n\n") (str/split body #"\n\n")))

;; WARN does not cover cases where there is only one \n before story
;; WARN as we cant have nil in the db, we mock it out with ""
(defn- body [post]
  (def p post)
  (let [body (-> (select/select (select/child (select/class :post-body)) post)
                 first :content body->str str/trim)
        [recipe prep story] (split-body body)]
    (def b body)
    (assoc post :cocktail/recipe recipe :cocktail/preparation (or prep " ") :cocktail/story (or story " "))))

(defn- nested-img [post]
  (-> (select/select (select/child (select/class :post-body) (select/tag :center) (select/tag :img)) post)
      first :attrs :src))

(defn- flat-img [post]
  (-> (select/select (select/child (select/class :post-body) (select/tag :img)) post)
      first :attrs :src))

(defn- img [post]
  (let [img (or (nested-img post)
                (flat-img post))]
    (util/?assoc post :cocktail/img img)))

(defn- parse-tag-by [pred k]
  (fn [post]
    (let [tag-set (->> (select/select (select/child (select/class :post-labels)) post)
                       first :content
                       (filter map?)
                       (map (comp str/lower-case first :content))
                       (filter pred)
                       (into #{}))]
      (if-not (empty? tag-set) (assoc post k tag-set) post))))

(defn- category? [tag]
  (re-matches #"\*.+" tag))

(defn- bar? [tag]
  (re-matches #"\#.+" tag))

(defn- ingredient? [tag]
  (re-matches #"\w.+" tag))

(defn- categories [post]
  ((parse-tag-by category? :cocktail/category) post))

(defn- bars [post]
  ((parse-tag-by bar? :cocktail/bar) post))

(defn- ingredients [post]
  ((parse-tag-by ingredient? :cocktail/ingredient) post))

(defn- prefix-ingredient [ingredient]
  (if-let [prefix (re-find #"\([\w\p{Punct}]+\)" ingredient)]
    (str/join " " (cons (str/replace prefix #"\(|\)" "")
                        (drop-last (str/split ingredient #" "))))
    ingredient))

(defn- prefix-ingredients [post]
  (update post :cocktail/ingredient #(into #{} (map prefix-ingredient %))))

(defn- fulltext [post]
  (let [fulltext (->> post vals (filter string?) (str/join " ") str/lower-case)]
    (assoc post :cocktail/fulltext fulltext)))

;; TODO broken, just filters everything away?
(defn- punch? [post min]
  (let [xf (comp (filter number?) (map read-string))]
    (< min (transduce xf + (str/split-lines (:cocktail/recipe post))))))

(defn- stirred? [post]
  (str/includes? (str/lower-case (:cocktail/fulltext post)) "stir"))

(defn- shaken? [post]
  (str/includes? (str/lower-case (:cocktail/fulltext post)) "shake"))

(defn- kind [post]
  (let [kind (cond (punch? post 50) "punch"
                   (stirred? post)  "stirred"
                   (shaken? post)   "shaken")]
    (util/?assoc post :cocktail/kind kind)))

(defn cocktail?
  "Assumes that all non-cocktail posts have a leading ':: ' in the title or are
   correctly tagged in the original source
   There might some collateral, but we accept that as we need correct cocktails."
  [{:cocktail/keys [title category]}]
  (and (set/subset? category #{"*hot" "*original" "*room temperature"})
       (not (re-find #"::" title))))

(defn post->cocktail [post]
  (-> post
      id url title author date body img categories bars ingredients prefix-ingredients fulltext kind
      (dissoc :type :attrs :tag :content)))

(def xf-cocktail
  (comp (map post->cocktail)
        (filter cocktail?)))

(defn posts->cocktails [posts]
  (into [] xf-cocktail posts))


(comment

  (posts->cocktails "posts.edn")
  
  (def p1 (->> "new-posts.edn" slurp read-string first)) ; new style
  (post->cocktail p1)
  (def p2 (->> "new-posts.edn" slurp read-string (drop 300) first)) ; old style
  (post->cocktail p2)

  (->> "posts.edn" slurp read-string posts->cocktails)

  )
