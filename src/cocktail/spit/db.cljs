(ns cocktail.spit.db
  (:require [cljs.reader :as reader]
            [cljs.spec.alpha :as s]
            [re-frame.core :as rf]))

;;;; specs

(s/def ::db (s/keys :req-un [::route ::active-cocktail ::collections ::strainer ::meta]))

(s/def ::active-cocktail map?)

(s/def ::meta (s/keys :req-un [::ingredients]))

(s/def ::collections (s/keys :req-un [::library ::menu]))
(s/def ::library set?)
(s/def ::menu set?)

(s/def ::strainer (s/keys :req-un [::ingredients ::cocktails ::search ::kind ::collection]))
(s/def ::ingredients (s/coll-of ::ingredient))
(s/def ::ingredient string?)
(s/def ::search string?)
(s/def ::collection set?)
(s/def ::kind set?)

(s/def ::cocktails (s/nilable (s/coll-of ::cocktail)))
(s/def ::cocktail map?)

;;;; app-db

;; TODO namespace these
(def default-db
  {:route nil
   :active-cocktail {} ; not sure where this should be
   :meta {:ingredients #{}}
   :collections {:menu #{} ;; TODO move this to be a set of ids
                 :library #{}}
   :strainer {:ingredients #{}
              :collection #{}
              :search ""
              :kind #{}
              :cocktails #{}}})

;;;; local-storage

(def ls-key "cocktail.slurp-collections")

(defn collections->local-storage [db]
  (.setItem js/localStorage ls-key (str (:collections db))))

(rf/reg-cofx
 :local-store-collections
 (fn [cofx _]
   (assoc cofx :local-store-collections
          (some->> (.getItem js/localStorage ls-key) (reader/read-string)))))
