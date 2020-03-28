(ns cocktail.stuff.util)

(defn gen-key
  "Generates a React key by hashing the str representation of `o`
  `rest`, and a random int to prevent collisions."
  [o & rest]
  (hash (str (rand-int 255) o rest)))

(defn ?assoc
  "assocs the `k` into the `m` if the `v` is truthy, otherwise returns `m`.
  NOTE: this version of ?assoc only does a single kv pair."
  [m k v]
  (if v (assoc m k v) m))

(defn map-map
  "Maps a `f` to all the v in `m`"
  [m f]
  (into {} (for [[k v] m] [k (f v)])))
