(ns cocktail.spit.helpers
  (:require [clojure.spec.alpha :as s]))

(defn gen-key
  "Generates a React key by hashing the str representation of `o`
  `rest`, and a random int to prevent collisions."
  [o & rest]
  (hash (str (rand-int 255) o rest)))

(defn ?assoc
  "assocs the `k` into the `m` if the `v` is non-nil, otherwise returns `m`.
  NOTE: this version of ?assoc only does a single kv pair."
  [m k v]
  (if v (assoc m k v) m))

(defn check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`.
  SOURCE: re-frame docs."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))
