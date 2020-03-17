(ns cocktail-slurp.parse
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

(defn- readable-date [date]
  (-> date (subs 2 10) (str/replace #"-" "")))

;; TODO make this an instant instead of a string
(defn- date [post]
  (let [date (-> (s/select (s/child (s/class :timestamp-link)) post)
                 first :content first :attrs :title)]
    (assoc post :date (readable-date date))))

(defn- body->str [body]
  (->> body (filter string?) (apply str)))

;; WARN does not cover cases where there is only one \n before story
;;      as we cant have nil in the db, we mock it out with ""
(defn- split-body [body]
  (str/split body #"\n\n"))

(defn- body [post]
  (let [body (-> (s/select (s/child (s/class :post-body)) post)
                 first :content body->str str/trim)
        [recipie prep story] (split-body body)]
    (assoc post :recipie recipie :preparation (or prep " ") :story (or story " "))))

(defn- nested-img [post]
  (-> (s/select (s/child (s/class :post-body) (s/tag :center) (s/tag :img)) post)
      first :attrs :src))

(defn- flat-img [post]
  (-> (s/select (s/child (s/class :post-body) (s/tag :img)) post)
      first :attrs :src))

(defn- img [post]
  (let [img (or (nested-img post)
                (flat-img post))]
    (if img (assoc post :img img) post)))

(defn- parse-tag-by [pred k]
  (fn [post]
    (let [tag-set (->> (s/select (s/child (s/class :post-labels)) post)
                      first :content
                      (filter map?)
                      (map (comp first :content))
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
  (if-let [prefix (re-find #"\(\w+\)" ingredient)]
    (str/join " " (cons (str/replace prefix #"\(|\)" "")
                        (drop-last (str/split ingredient #" "))))
    ingredient))

(defn- prefix-ingredients [post]
  (update post :ingredients #(into #{} (map prefix-ingredient %))))

(defn cocktail?
  "Assumes that all non-cocktail posts have a leading ':: ' in the title.
   There might some collateral, but we accept that as we need corrects cocktails."
  [{:keys [title]}]
  (not (re-find #"::" title)))

(defn post->cocktail [post]
  (-> post id url title author date body img categories bars ingredients prefix-ingredients
      (dissoc :type :attrs :tag :content)))

(def xf-cocktail
  (comp (map post->cocktail)
        (filter cocktail?)))

(defn posts->cocktails [posts]
  (->> posts slurp read-string (into [] xf-cocktail)))
