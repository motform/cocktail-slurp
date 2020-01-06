(ns cocktail-slurp.scrpe
  (:require [hickory.core :as h]
            [hickory.select :as s]))

(defn- page [url]
  (-> url slurp h/parse h/as-hickory))

(defn- posts [page]
  (s/select (s/child (s/class :post)) page))

(defn- next-page [page]
  (str (-> (s/select (s/child (s/class "blog-pager-older-link")) page)
           first :attrs :href)
       "9")) ;; [HACK] add 9 to url to get more posts per page

;; FIXME for some reason, there is a single result with :http: in a k
;;       this breaks ~read-string~ parsing, so it has be removed (right now by hand)
(defn scrape! [url]
  (loop [url url pages []]
    (if-let [cursor (try (page url) (catch Exception _ nil))]
      (do (println url)
          (recur (next-page cursor)
                 (concat pages (posts cursor))))
      (spit "resources/posts.edn" (pr-str (into [] pages))))))
