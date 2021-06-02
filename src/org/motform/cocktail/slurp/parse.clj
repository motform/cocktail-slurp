(ns org.motform.cocktail.slurp.parse
  (:require [clojure.instant :as instant]
            [clojure.set     :as set]
            [clojure.string  :as str]
            [hickory.select  :as select]
            [org.motform.cocktail.stuff.util :as util]))

(defn id [post]
  (assoc post :cocktail/id
         (-> (select/select (select/child (select/attr :name)) post)
             first :attrs :name)))

(defn url [post]
  (assoc post :cocktail/url
         (-> (select/select (select/child (select/class :timestamp-link)) post)
             first :attrs :href)))

(defn title [post]
  (assoc post :cocktail/title
         (-> (select/select (select/child (select/class :post-title)) post)
             first :content second :content first)))

(defn author [post]
  (assoc post :cocktail/author
         (-> (select/select (select/child (select/class :post-author)) post)
             first :content second :content first)))

;; TODO make this an instant instead of a string
(defn date [post]
  (let [date (-> (select/select (select/child (select/class :timestamp-link)) post)
                 first :content first :attrs :title)]
    (assoc post :cocktail/date (instant/read-instant-date date))))

(defn flatten-anchors
  "Flatten a vec of nodes Hickory into a string.  Nodes are flattened
  tuples of [str {meta}], so sometimes one has so extract the string
  from the meta map."
  [node]
  (if (string? node)
    (str/trim node)
    (let [tag (:tag node)]
      (cond (= :br tag) "\n"
            (= :p  tag) (apply str (map flatten-anchors (:content node)))
            (= :i  tag) (str " <em>" (-> node :content first) "</em> ")
            (= :a  tag) (str " <a href=" (-> node :attrs :href) ">" (-> node :content first) "</a> ")
            (:attrs node)       ""
            (:content node)     "\n\n"
            :else (or (-> node :content first) "\n")))))

(defn body->str [body]
  (->> body (map flatten-anchors) (filter string?) (apply str)))

(defn split-body [body]
  (map #(str/replace % #"  " "\n\n") (str/split body #"\n\n")))

(defn leading-newline? [body]
  (if (= "\n" (first body))
    (rest body)
    body))

(defn body [post]
  (let [body (-> (select/select (select/child (select/class :post-body)) post)
                 first :content leading-newline? body->str str/trim)
        [recipe prep story] (split-body body)]
    (assoc post :cocktail/recipe recipe :cocktail/preparation (or prep " ") :cocktail/story (or story " "))))

(defn nested-img [post]
  (-> (select/select (select/child (select/class :post-body) (select/tag :center) (select/tag :img)) post)
      first :attrs :src))

(defn flat-img [post]
  (-> (select/select (select/child (select/class :post-body) (select/tag :img)) post)
      first :attrs :src))

(defn img [post]
  (let [img (or (nested-img post)
                (flat-img post))]
    (util/?assoc post :cocktail/img img)))

(defn parse-tag-by [pred k]
  (fn [post]
    (let [tag-set (->> (select/select (select/child (select/class :post-labels)) post)
                       first :content
                       (filter map?)
                       (map (comp str/lower-case first :content))
                       (filter pred)
                       (into #{}))]
      (if-not (empty? tag-set) (assoc post k tag-set) post))))

(defn category? [tag]
  (re-matches #"\*.+" tag))

(defn bar? [tag]
  (re-matches #"\#.+" tag))

(defn ingredient? [tag]
  (re-matches #"\w.+" tag))

(defn categories [post]
  ((parse-tag-by category? :cocktail/category) post))

(defn bars [post]
  ((parse-tag-by bar? :cocktail/bar) post))

(defn ingredients [post]
  ((parse-tag-by ingredient? :cocktail/ingredient) post))

(defn prefix-ingredient [ingredient]
  (if-let [prefix (re-find #"\([\w\p{Punct}]+\)" ingredient)]
    (str/join " " (cons (str/replace prefix #"\(|\)" "")
                        (drop-last (str/split ingredient #" "))))
    ingredient))

(defn prefix-ingredients [post]
  (update post :cocktail/ingredient #(into #{} (map prefix-ingredient %))))

(defn fulltext [post]
  (let [fulltext (->> post vals (filter string?) (str/join " ") str/lower-case)]
    (assoc post :cocktail/fulltext fulltext)))

;; TODO broken, just filters everything away?
(defn punch? [post min]
  (let [xf (comp (filter number?) (map read-string))]
    (< min (transduce xf + (str/split-lines (:cocktail/recipe post))))))

(defn stirred? [post]
  (str/includes? (str/lower-case (:cocktail/fulltext post)) "stir"))

(defn shaken? [post]
  (str/includes? (str/lower-case (:cocktail/fulltext post)) "shake"))

(defn kind [post]
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
  (def p1 (->> "resources/edn/posts.edn" slurp read-string first))
  (post->cocktail p1)
  (def p2 (->> "resources/edn/posts.edn" slurp read-string (drop 38) first))
  (post->cocktail p2)

  (->> "new-posts.edn" slurp read-string posts->cocktails)

  )
