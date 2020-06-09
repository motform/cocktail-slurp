(ns cocktail.spit.db
  (:require [cljs.reader :as reader]
            [cljs.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [re-frame.core :as rf]))

;;;; specs

(s/def ::db (s/keys :req-un [::active-page ::active-cocktail ::collections ::strainer ::metadata]))

(s/def ::active-page #{:cocktails :cocktail :library :menu :about})
(s/def ::active-cocktail map?)

(s/def ::metadata (s/keys :req-un [::ingredients]))

(s/def ::collections (s/keys :req-un [::library ::menu]))
(s/def ::library (s/keys :req-un [::cocktails]))
(s/def ::menu (s/keys :req-un [::cocktails]))

(s/def ::strainer (s/keys :req-un [::ingredients ::cocktails ::search ::type]))
(s/def ::ingredients (s/coll-of ::ingredient))
(s/def ::ingredient string?)

;; NOTE this should really always be a set, to be compatible with lib and menu
;; TODO spec the cocktail map, or not, depending on if we strip and serve
;; only the essential over the wire. Old form:
;; (s/def ::cocktail (s/keys :req-un [(keys (first data/cocktails))]))

(s/def ::cocktails (s/coll-of ::cocktail))
(s/def ::cocktail map?)

;;;; app-db

(def default-db
  {:active-page :cocktails
   :active-cocktail {} ; not sure where this should be
   :meta {:ingredients #{}}
   :collections {:menu {:cocktails #{}}
                 :library {:cocktails #{}}}
   :strainer {:ingredients #{}
              :search ""
              :type #{}
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
