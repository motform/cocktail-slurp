(ns org.motform.cocktail.stuff.illustration)

;; TODO move into a an .edn file and render serverside
(def ingredient->color
  {"mirto" "blue"
   "amaretto" "brown"
   "chinato" "brown"
   "cocoa" "brown"
   "drambuie" "brown"
   "sherry" "brown"
   "tea" "brown"
   "applejack" "darkgoldenrod"
   "aquavit" "darkgoldenrod"
   "benedictine" "darkgoldenrod"
   "brandy" "darkgoldenrod"
   "calvados" "darkgoldenrod"
   "cognac" "darkgoldenrod"
   "rum" "darkgoldenrod"
   "scotch" "darkgoldenrod"
   "whiskey" "darkgoldenrod"
   "chartreuse" "green"
   "falernum" "green"
   "herbsainte" "green"
   "absinthe" "lightgreen"
   "pastis" "lightgreen"
   "aperol" "orange"
   "bauchant" "orange"
   "beer" "orange"
   "cointreau" "orange"
   "quinquina" "orangered"
   "amaro" "red"
   "averna" "red"
   "campari" "red"
   "cynar" "red"
   "dubonnet" "red"
   "grenadine" "red"
   "madeira" "red"
   "cream" "white"
   "egg" "white"
   "kahlua" "white"
   "becherovka" "whitesmoke"
   "cachaça" "whitesmoke"
   "curacao" "whitesmoke"
   "fernet-branca" "whitesmoke"
   "genever" "whitesmoke"
   "gin" "whitesmoke"
   "grappa" "whitesmoke"
   "kümmel" "whitesmoke"
   "lillet" "whitesmoke"
   "malört" "whitesmoke"
   "maraschino" "whitesmoke"
   "mezcal" "whitesmoke"
   "orgeat" "whitesmoke"
   "pisco" "whitesmoke"
   "soda" "whitesmoke"
   "sugar" "whitesmoke"
   "suze" "whitesmoke"
   "tequila" "whitesmoke"
   "vodka" "whitesmoke"
   "champagne" "yellow"
   "galliano" "yellow"
   "honey" "yellow"
   "strega" "yellow"
   "licor 43" "orangered"
   "batavia arrack" "whitesmoke"
   "ginger beer" "orange"
   "aged bitters" "darkgoldenrod"
   "angostura bitters" "brown"
   "aromatic bitters" "darkgoldenrod"
   "gammeldansk bitters" "whitesmoke"
   "mole bitters" "brown"
   "orange bitters" "orange"
   "other bitters" "brown"
   "peychaud's bitters" "black"
   "cherry brandy" "darkgoldenrod"
   "rock candy syrup" "whitesmoke"
   "green chartreuse" "green"
   "yellow chartreuse" "yellow"
   "apple cider" "yellow"
   "creme de violette" "purple"
   "creme de banane" "yellow"
   "creme de cassis" "purple"
   "creme de noyaux" "red"
   "creme de rose" "pink"
   "creme de peche" "orange"
   "creme de cacao" "brown"
   "pineau des charentes" "darkgoldenrod"
   "pimento dram" "black"
   "punt e mes" "red"
   "passion fruit syrup" "yellow"
   "st. germain" "green"
   "sloe gin" "purple"
   "grapefruit juice" "orange"
   "lemon juice" "yellow"
   "lime juice" "limegreen"
   "orange juice" "orange"
   "pineapple juice" "yellow"
   "apricot liqueur" "orange"
   "ginger liqueur" "orange"
   "honey liqueur" "yellow"
   "pear liqueur" "green"
   "walnut liqueur" "brown"
   "pimm's no. 1" "red"
   "amaro nonino" "red"
   "amer picon" "red"
   "ruby port" "red"
   "tawny port" "red"
   "white port" "white"
   "swedish punsch" "darkgoldenrod"
   "white rum" "whitesmoke"
   "creole shrub" "orange"
   "cinnamon simple syrup" "brown"
   "mint simple syrup" "whitesmoke"
   "other simple syrup" "whitesmoke"
   "vanilla simple syrup" "whitesmoke"
   "gum syrup" "whitesmoke"
   "maple syrup" "brown"
   "pineapple syrup" "yellow"
   "raspberry syrup" "pink"
   "simple syrup" "whitesmoke"
   "bianco vermouth" "whitesmoke"
   "dry vermouth" "yellow"
   "rose vermouth" "pink"
   "sweet vermouth" "red"
   "infused vodka" "whitesmoke"
   "bourbon whiskey" "darkgoldenrod"
   "rye whiskey" "darkgoldenrod"
   "egg white" "white"
   "rose wine" "pink"
   "white wine" "whitesmoke"
   "red wine" "red"})

(defn background-rect [ingredient w h]
  [:rect {:width w :height h
          :fill (ingredient->color ingredient)
          :fill-opacity "0.3"}])

(defn illustration [{ingredients :cocktail/ingredient} h]
  (let [w "100%"]
    [:svg.illustration {:style {:height h :width w}}
     (for [ingredient ingredients]
       (background-rect ingredient w h))]))
