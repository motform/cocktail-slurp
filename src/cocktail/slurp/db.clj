(ns cocktail.slurp.db
  (:require [cocktail.slurp.parse :as parse]
            [datomic.api :as d]))

;;; db

(def *conn (atom nil))

(defn- init-conn! [uri]
  (reset! *conn (d/connect uri)))

(defn init-db!
  "Initialize and populate the in-memory Datomic db.
   Expects `posts` to be the relative path to an .edn of scraped posts"
  [{:keys [uri schema posts]}]
  (d/create-database uri)
  (init-conn! uri)
  @(d/transact @*conn (-> schema slurp read-string))
  @(d/transact @*conn (parse/posts->cocktails posts)))

;;; queries

;; NOTE returns nil and not [] on fail
(defn paginate [start limit q & args]
  (let [result (apply q args)
        stop (+ start limit)]
    (when (seq result)
      (if (< stop (count result))
        (subvec result start stop)
        (subvec result start)))))

(defn cocktail-by-id [id]
  (d/pull (d/db @*conn) '[:id :title :recipe :preparation :ingredients] [:id id]))

(defn cocktail-feed []
  (d/q '[:find [(pull ?e [:id :title :recipe :preparation :ingredients]) ...]
         :where [?e :id]]
       (d/db @*conn)))

(def base-query
  '{:query {:find [(pull ?e [:id :title])]
            :in [$]
            :where []}
    :args []})

(defn- simple-query [q k s xs]
  (-> q
      (update-in [:query :in] conj [s '...])
      (update-in [:query :where] conj ['?e k s])
      (update-in [:args] conj xs)))

(defn- fn-query [q k s f db xs]
  (-> q
      (update-in [:query :in] conj [s '...])
      (update-in [:query :where] conj [`(~f ~db ~k ~s) '[[?e ?n]]])
      (update-in [:args] conj xs)))

(defn- parse-strainer [{:keys [:ingredients :search :type]}]
  (cond-> base-query
    ingredients (simple-query :ingredients '?ingredients ingredients)
    type (simple-query :type '?type type)
    search (fn-query :fulltext '?fulltext 'fulltext '$ search)))

(defn strain [strainer]
  (let [{:keys [query args]} (parse-strainer strainer)]
    (apply d/q query (d/db @*conn) args)))


(comment
  ;; datomic
  (init-db! {:uri "datomic:mem://cocktail.slurp"
             :posts "posts.edn"
             :schema "resources/edn/cocktail-schema.edn"})

  (d/delete-database "datomic:mem://cocktail.slurp")

  (strain {:ingredients ["rum" "cream"] :search ["russian"]})

  ;; export the cocktails
  (spit "resources/edn/formatted-posts.edn" (pr-str (parse/posts->cocktails "posts.edn"))))
