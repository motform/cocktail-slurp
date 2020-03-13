(ns cocktail-slurp.parse
  (:require [clojure.string :as str]
            [hickory.select :as s]))

(declare id url date title body img author categories bars ingredients prefix-ingredients)


(defn post->cocktail [post]
  (-> post id url title author date body img categories bars ingredients prefix-ingredients
      (dissoc :type :attrs :tag :content)))

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

(defn- date [post]
  (let [date (-> (s/select (s/child (s/class :timestamp-link)) post)
                 first :content first :attrs :title)]
    (assoc post :date (readable-date date))))

(defn- body->str [body]
  (->> body (filter string?) (apply str)))

;; WARN does not cover cases where there is only one \n before story
(defn- split-body [body]
  (str/split body #"\n\n"))

(defn- body [post]
  (let [body (-> (s/select (s/child (s/class :post-body)) post)
                 first :content body->str str/trim)
        [recipie prep story] (split-body body)]
    (assoc post :recipie recipie :preparation prep :story story)))

(defn- nested-img [post]
  (-> (s/select (s/child (s/class :post-body) (s/tag :center) (s/tag :img)) post)
      first :attrs :src))

(defn- flat-img [post]
  (-> (s/select (s/child (s/class :post-body) (s/tag :img)) post)
      first :attrs :src))

(defn- img [post]
  (assoc post :img (or (nested-img post)
                       (flat-img post))))

(defn- parse-tag-by [pred k]
  (fn [post]
    (assoc post k
           (->> (s/select (s/child (s/class :post-labels)) post)
               first :content
               (filter map?)
               (map (comp first :content))
               (filter pred)
               (into #{})))))

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

(defn prefix-ingredients [post]
  (update post :ingredients #(into #{} (map prefix-ingredient %))))
