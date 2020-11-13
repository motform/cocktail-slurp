(ns cocktail.stuff.util
  (:require [clojure.string :as str]))

(defn gen-key
  "Generates a React key by hashing the str representation of `o`
  `rest`, and a random int to prevent collisions."
  [o & rest]
  (hash (str (rand-int 255) o rest)))

(defn ?assoc
  "Associates the `k` into the `m` if the `v` is truthy, otherwise returns `m`.
  NOTE: this version of ?assoc only does a single kv pair."
  [m k v]
  (if v (assoc m k v) m))

(defn ?update
  "Update `k` with `f` in `m`, the `k` exists, otherwise returns `m`. "
  [m k f & args]
  (if (k m) (apply update m k f args) m))

(defn map-map
  "Maps a `f` to all the v in `m`"
  [m f]
  (into {} (for [[k v] m] [k (f v)])))

(defn toggle
  "Toggle membership of `x` in `set`"
  [set x]
  (if (set x) (disj set x) (conj set x)))

(defn remove-empty [m]
  (into {} (remove (comp empty? second) m)))

(defn ?subvec [v start end]
  (if (> end (count v))
    (subvec v start)
    (subvec v start end)))

(defn measurement? [s]
  (let [measurements #{"oz" "jigger" "ml" "cl" "dl" "dash" "tsp" "tbsp" "scant" "spoon"
                       "quart" "bsp" "heaping" "whole" "Whole" "drop" "drops"}]
    (or (measurements (str/lower-case s))
        (re-matches #"[\d/]+" s)
        (re-matches #"\d+\-\d+" s))))

(defn abbrev-measurement [measurement]
  (case (str/lower-case measurement)
    "jigger" "jig"
    "dash" "ds"
    "scant" "s" 
    "heaping" "h"
    "drops"  "dr" "drop"  "dr"
    "spoon" "spn"
    "whole" ""
    (str/lower-case measurement)))

(defn split-ingredient [ingredient]
  (let [words (str/split ingredient " ")]
    (assoc {} :measurement (->> words
                               (take-while measurement?)
                               (map abbrev-measurement)
                               (str/join " "))
           :name (str/join " " (drop-while measurement? words)))))
