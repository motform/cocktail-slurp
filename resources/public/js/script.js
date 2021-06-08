// mobile filter menu toggle
let toggle = false;

document.getElementById("ftoggle").onclick = () => {
  document.getElementById("strainer").style.display = (toggle ? "none" : "flex");
  document.getElementById("cards").style.display    = (toggle ? "grid" : "none");
  toggle = !toggle;
}

// auto-filtering ingredients

const rest = ([car, ...cdr]) => cdr;

const partition = (xs) => {
  return xs.reduce((acc, x, i, xs) => {
    if (i % 2 === 0) acc.push(xs.slice(i, i + 2));
    return acc
  }, []);
}

const ingredientPairs = ingredientSection => partition(rest(Array.from(ingredientSection.children)));

const isActiveSection = ([ingredientSection]) => ingredientSection
      .filter(([checkbox, ingredient]) => possibleIngredients[ingredient.htmlFor])
      .length;

function checkIngredientSections() {
  for ([category, section] of ingredientSections) {
    category.style.display = (isActiveSection([section]) ? "flex" :  "none");
  }
}

function checkIngredients() {
  if (HTTPRequest.readyState === XMLHttpRequest.DONE) {
    possibleIngredients = JSON.parse(HTTPRequest.response);

    for ([checkbox, label] of ingredientLabels) {
      const possibleCocktails = possibleIngredients[label.htmlFor];
      label.style.display     = (possibleCocktails ? "block" : "none");
      [possibleCocktailCount] = label.children;
      possibleCocktailCount.innerText = (possibleCocktails  ? " " + possibleCocktails : "");
    }

    checkIngredientSections();
  }
}

function requestIngredientCheck() {
  if (selectedIngredients.size) {
    let params = new URLSearchParams(); // this could be a json array, but I like my query strings
    Array.from(selectedIngredients).map(i => params.append("ingredient", i));

    HTTPRequest = new XMLHttpRequest();
    HTTPRequest.onreadystatechange = checkIngredients;
    HTTPRequest.open("GET", "/possible-ingredients" + "?" + params.toString(), true);
    HTTPRequest.send();
  } else {
    for ([checkbox, label] of ingredientLabels) {
      label.style.display = "block";
      [possibleCocktailCount] = label.children;
      possibleCocktailCount.innerText = "";
    }
  }
}

function onIngredientClick(ingredient) {
  const ingredientName = ingredient.target.htmlFor;
  selectedIngredients.has(ingredientName)
    ? selectedIngredients.delete(ingredientName)
    : selectedIngredients.add(ingredientName);
  requestIngredientCheck()
}

function checkChecked() {
  const checked = ingredientLabels
        .filter(i => i[0].checked)
        .map(i => i[1].htmlFor);
  selectedIngredients = new Set(checked);
  requestIngredientCheck();
}

let selectedIngredients = new Set();
let possibleIngredients = {};
let HTTPRequest;

let ingredientSections = Array.from(document.getElementsByClassName("ingredients"));
const ingredientLabels = partition(ingredientSections
                                   .map(is => rest(Array.from(is.children)))
                                   .flat());

ingredientSections = ingredientSections.map(is => [is, ingredientPairs(is)]);
ingredientLabels.map(i => i[1].onclick = onIngredientClick)
checkChecked(); // this results in an initial flash, suck it up React
