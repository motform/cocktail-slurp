(ns org.motform.cocktail.slurp.scrape
  "There are two kinds of main scraping actions:
  `scrape-from-scratch!` and `scrape-new-cocktails!`.

  `scrape-from-scratch!` is for building a new databases, scraping
  all the cocktails it can get its thirsty hands on.

  `scrape-new-cocktails!` is for updating an existing database. There
  was a choice of either:
    A. Checking for cocktails any time someone makes a request.
    B. Checking for cocktails on an interval.
  Ultimately, I choose B as it is simpler and does not cause overhead
  for all requests."
  (:require [clojure.java.io :as io]
            [clojure.pprint  :as pp]
            [chime.core      :as chime]
            [hickory.core    :as hickory]
            [hickory.select  :as select]
            [mount.core      :as mount]
            [org.motform.cocktail.slurp.db    :as db]
            [org.motform.cocktail.slurp.parse :as parse])
  (:import  [java.time LocalTime Period ZonedDateTime ZoneId Instant Duration]
            [java.io File]))

(def every-day-9am-in-boston
  (chime/periodic-seq
   (-> (LocalTime/of 9 0 0)
       (.adjustInto (ZonedDateTime/now (ZoneId/of "America/New_York")))
       .toInstant)
   (Period/ofDays 1)))

(defn id [post]
  (-> (select/select (select/child (select/attr :name)) post)
      first :attrs :name))

(defn page [url]
  (with-open [reader (io/reader url)]
    (hickory/as-hickory (hickory/parse (slurp reader)))))

(defn posts [page]
  (select/select (select/child (select/class :post)) page))

(defn next-page [page]
  (str (-> (select/select (select/child (select/class "blog-pager-older-link")) page)
           first :attrs :href)
       "9")) ;; HACK add 9 to url to get more posts per page

(defn- write-edn [file-name edn]
  (spit (File. file-name)
        (with-out-str (pp/write edn))))

;; FIXME for some reason, there is a always a :http: and :http2: key in the spit edn,
;;       this breaks ~read-string~ parsing, so it has be removed (right now by hand)
(defn scrape-from-scratch! [url path]
  (loop [url url
         pages []]
    (if-let [cursor (try (page url)
                         (catch Exception e (println "Scraping stopped (could be both a fail or a finalisation, who knows?). Reason:" e)))]
      (do (println url)
          (recur (next-page cursor)
                 (concat pages (posts cursor))))
      (write-edn path (into [] pages)))))

;; TODO This should also update the underlying .edn 
(defn scrape-new-cocktails! [url conn]
  (let [cocktails (-> url page posts parse/posts->cocktails)]
    (doseq [{:keys [cocktail/id] :as cocktail} cocktails]
      (when-not (:db/id (db/cocktail-by-id id))
        (db/add-cocktail conn cocktail)))))

(defn- every-x-seconds [seconds]
  (chime/periodic-seq (Instant/now) (Duration/ofSeconds seconds)))

(mount/defstate scraper
  :start (chime/chime-at every-day-9am-in-boston
                         (fn [time]
                           (println "Scraping new cocktails at" time)
                           (scrape-new-cocktails! "https://cocktailvirgin.blogspot.com" db/conn))
                         {:on-finished #(println "Scraped new cocktails!")})
  :stop  (.close scraper))

(comment
  (scrape-from-scratch!  "https://cocktailvirgin.blogspot.com" "resources/edn/posts.edn")
  (scrape-new-cocktails! "https://cocktailvirgin.blogspot.com" db/conn))

