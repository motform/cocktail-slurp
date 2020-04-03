(ns cocktail.stuff.util
  (:require #?(:cljs [cognitect.transit :as transit])
            #?(:clj  [muuntaja.core :as muuntaja])))

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

(defn remove-empty [m]
  (into {} (remove (comp empty? second) m)))

(defn ->transit+json [data]
  #?(:cljs (transit/write (transit/writer :json) data)
     :clj (muuntaja/encode "application/transit+json" data)))
