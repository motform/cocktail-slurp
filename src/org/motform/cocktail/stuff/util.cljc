(ns org.motform.cocktail.stuff.util
  (:require [clojure.string :as str]))

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
    "jigger"  "jig"
    "dash"    "ds"
    "scant"   "s" 
    "heaping" "h"
    "drops"   "dr"
    "drop"    "dr"
    "spoon"   "spn"
    "whole"   ""
    (str/lower-case measurement)))

(defn split-ingredient [ingredient]
  (let [words (str/split ingredient #" ")]
    (assoc {} :measurement (->> words (take-while measurement?) (map abbrev-measurement) (str/join " "))
           :name (->> words (drop-while measurement?) (str/join " ")))))


(def ingredients
  {:base    ["applejack" "aquavit" "batavia arrack" "bourbon whiskey" "brandy" "cachaça" "calvados" "cognac" "genever" "gin" "grappa" "mezcal" "pisco" "rum" "rye whiskey" "scotch" "tequila" "vodka" "whiskey" "white rum"]
   :bitter  ["aged bitters" "angostura bitters" "aromatic bitters" "herbsainte" "mole bitters" "orange bitters"  "peychaud's bitters" "other bitters"]
   :liqueur ["absinthe" "amaretto" "amaro" "amaro nonino" "amer picon" "aperol" "apricot liqueur" "averna" "bauchant" "becherovka" "benedictine" "campari" "chartreuse" "cherry brandy" "cointreau" "creme de banane" "creme de cacao" "creme de cassis" "creme de noyaux" "creme de rose" "creme de rose" "creme de violette" "creole shrub" "curacao" "cynar" "drambuie" "falernum" "fernet-branca" "galliano" "gammeldansk bitters" "ginger liqueur" "green chartreuse" "honey liqueur" "infused vodka" "kahlua" "kümmel" "licor 43" "malört" "maraschino" "mirto" "pastis" "pear liqueur" "pimento dram" "pimm's no. 1" "pineau des charentes" "sloe gin" "st. germain" "strega" "suze" "swedish punsch" "walnut liqueur" "yellow chartreuse"]
   :juice   ["grapefruit juice" "lemon juice" "lime juice" "orange juice" "pineapple juice"]
   :syrup   ["cinnamon simple syrup" "grenadine" "gum syrup" "maple syrup" "mint simple syrup" "orgeat"  "passion fruit syrup" "pineapple syrup" "raspberry syrup" "rock candy syrup" "simple syrup" "vanilla simple syrup" "other simple syrup"]
   :wine    ["bianco vermouth" "dry vermouth" "rose vermouth" "sweet vermouth"  "chinato"  "dubonnet" "lillet"  "punt e mes" "quinquina" "madeira" "sherry" "ruby port"  "tawny port" "white port" "champagne" "red wine" "rose wine" "white wine"]
   :pantry  ["apple cider" "beer" "cocoa" "cream" "creme de peche" "egg" "egg white" "ginger beer" "honey" "soda" "sugar" "tea"]})
