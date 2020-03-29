(ns cocktail.slurp.parse
  (:require [clojure.string :as str]
            [hickory.select :as s]))

;; TODO add parsing for type #{:strained :stirred :punch}

(defn- id [post]
  (assoc post :id
         (-> (s/select (s/child (s/attr :name)) post)
             first :attrs :name)))

(defn- url [post]
  (assoc post :url
         (-> (s/select (s/child (s/class :timestamp-link)) post)
             first :attrs :href)))

(defn- title [post]
  (assoc post :title
         (-> (s/select (s/child (s/class :post-title)) post)
             first :content second :content first)))

(defn- author [post]
  (assoc post :author
         (-> (s/select (s/child (s/class :post-author)) post)
             first :content second :content first)))

(defn- readable-date
  "Formats long date string to YYMMDD"
  [date]
  (-> date (subs 2 10) (str/replace #"-" "")))

;; TODO make this an instant instead of a string
(defn- date [post]
  (let [date (-> (s/select (s/child (s/class :timestamp-link)) post)
                 first :content first :attrs :title)]
    (assoc post :date (readable-date date))))

(defn- flatten-anchors
  "Takes a vec of text nodes Hickory has extracted from a <p> and
  flattens the anchors into their `:content`. Nodes have are pairs
  str {meta} in a vec, with :content holding the anchor str in
  cases of that tag, which we would otherwise miss by just
  filtering for the strings. Does not preserve URLs."
  [x]
  (cond 
    (string? x) x
    (map? x) (-> x :content first)
    :else (throw (Exception. "invalid input to `flatten-anchors`"))))

(defn- body->str [body]
  (->> body (map flatten-anchors) (apply str)))

(defn- split-body [body]
  (map #(str/replace % #"  " "\n\n") (str/split body #"\n\n")))

;; WARN does not cover cases where there is only one \n before story
;; WARN as we cant have nil in the db, we mock it out with ""
(defn- body [post]
  (let [body (-> (s/select (s/child (s/class :post-body)) post)
                 first :content body->str str/trim)
        [recipe prep story] (split-body body)]
    (assoc post :recipe recipe :preparation (or prep " ") :story (or story " "))))

(defn- nested-img [post]
  (-> (s/select (s/child (s/class :post-body) (s/tag :center) (s/tag :img)) post)
      first :attrs :src))

(defn- flat-img [post]
  (-> (s/select (s/child (s/class :post-body) (s/tag :img)) post)
      first :attrs :src))

(defn- img [post]
  (let [img (or (nested-img post)
                (flat-img post))]
    (if img (assoc post :img img) post))) ;; TODO refactor into ?assoc

(defn- parse-tag-by [pred k]
  (fn [post]
    (let [tag-set (->> (s/select (s/child (s/class :post-labels)) post)
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
  ((parse-tag-by category? :categories) post))

(defn- bars [post]
  ((parse-tag-by bar? :bars) post))

(defn- ingredients [post]
  ((parse-tag-by ingredient? :ingredients) post))

(defn- prefix-ingredient [ingredient]
  (if-let [prefix (re-find #"\([\w\p{Punct}]+\)" ingredient)]
    (str/join " " (cons (str/replace prefix #"\(|\)" "")
                        (drop-last (str/split ingredient #" "))))
    ingredient))

(defn- prefix-ingredients [post]
  (update post :ingredients #(into #{} (map prefix-ingredient %))))

(defn- fulltext [post]
  (let [fulltext (->> post vals (filter string?) (str/join " ") str/lower-case)]
    (assoc post :fulltext fulltext)))

;; TODO add filtering by category (should not be essay and stuff)
(defn cocktail?
  "Assumes that all non-cocktail posts have a leading ':: ' in the title.
   There might some collateral, but we accept that as we need corrects cocktails."
  [{:keys [title]}]
  (not (re-find #"::" title)))

(defn post->cocktail [post]
  (-> post id url title author date body img categories bars ingredients prefix-ingredients fulltext
      (dissoc :type :attrs :tag :content)))

(def xf-cocktail
  (comp (map post->cocktail)
        (filter cocktail?)))

(defn posts->cocktails [posts]
  (->> posts slurp read-string (into [] xf-cocktail)))
