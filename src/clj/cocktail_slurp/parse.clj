(ns cocktail-slurp.parse
  (:require [clojure.string :as string]
            [hickory.select :as s]))

;; TODO splits tags from recipe into types
(defn category? [tag]
  (re-matches #"\*.+" tag))

(defn bar? [tag]
  (re-matches #"\#.+" tag))

(defn ingredient? [tag]
  (re-matches #"\w.+" tag))

(declare id url date title author categories bars ingredients body)

(defn post->map [post]
  (-> post id url title author date body categories bars ingredients
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
             first :content second :content first keyword)))

(defn- date [post]
  (assoc post :date
         (-> (s/select (s/child (s/class :timestamp-link)) post)
             first :content first :attrs :title)))

(defn- body->str [body]
  (->> body (filter string?) (apply str)))

(defn- body [post]
  (assoc post :body
         (-> (s/select (s/child (s/class :post-body)) post)
             first :content body->str string/trim)))

(defn- parse-tag-by [pred k]
  (fn [post]
    (assoc post k
           (->> (s/select (s/child (s/class :post-labels)) post)
               first :content
               (filter map?)
               (map (comp first :content)) ;; NOTE tags can include spaces and are thus unfit for kw
               (filter pred)
               (into #{})))))

(defn- categories [post]
  ((parse-tag-by category? :categories) post))

(defn- bars [post]
  ((parse-tag-by bar? :bars) post))

(defn- ingredients [post]
  ((parse-tag-by ingredient? :ingredient) post))
