(ns org.motform.cocktail.slurp.scrape
  (:require [hickory.core   :as hickory]
            [hickory.select :as select]))

(defn- page [url]
  (-> url slurp hickory/parse hickory/as-hickory))

(defn- posts [page]
  (select/select (select/child (select/class :post)) page))

(defn- next-page [page]
  (str (-> (select/select (select/child (select/class "blog-pager-older-link")) page)
           first :attrs :href)
       "9")) ;; HACK add 9 to url to get more posts per page

;; FIXME for some reason, there is a single result with :http: in a k
;;       this breaks ~read-string~ parsing, so it has be removed (right now by hand)
(defn scrape! [url path]
  (loop [url url pages []]
    (if-let [cursor (try (page url) (catch Exception _ nil))]
      (do (println url)
          (recur (next-page cursor)
                 (concat pages (posts cursor))))
      (spit path (pr-str (into [] pages))))))
