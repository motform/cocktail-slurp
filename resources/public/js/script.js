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

const isActiveSection = ingredientSection => ingredientSection
      .filter(i => possibleIngredients.has(i[1].textContent))
      .length;

function checkIngredientSections() {
  ingredientSections.map(is =>
    (isActiveSection(is[1])
     ? is[0].style.display = "flex"
     : is[0].style.display = "none")
  );
}

function checkIngredients() {
  if (HTTPRequest.readyState === XMLHttpRequest.DONE) {
    possibleIngredients = new Set(JSON.parse(HTTPRequest.response));

    if (possibleIngredients.size) {
      ingredientLabels.map(i => i[1].style.display = (possibleIngredients.has(i[1].textContent) ? "block" : "none"));
      checkIngredientSections();
    } else {
      ingredientLabels.map(i => i[1].style.display = "block");
    }      
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
    ingredientLabels.map(i => i[1].style.display = "block");
  }
}

function onIngredientClick(ingredient) {
  const ingredientName = ingredient.target.textContent;
  selectedIngredients.has(ingredientName)
    ? selectedIngredients.delete(ingredientName)
    : selectedIngredients.add(ingredientName);
  requestIngredientCheck()
}

function checkChecked() {
  const checked = ingredientLabels
        .filter(i => i[0].checked)
        .map(i => i[1].textContent);
  selectedIngredients = new Set(checked);
  requestIngredientCheck();
}

let selectedIngredients = new Set();
let possibleIngredients = new Set();
let HTTPRequest;

let ingredientSections = Array.from(document.getElementsByClassName("ingredients"));
const ingredientLabels = partition(ingredientSections
                                   .map(is => rest(Array.from(is.children)))
                                   .flat());

ingredientSections = ingredientSections.map(is => [is, ingredientPairs(is)]);
ingredientLabels.map(i => i[1].onclick = onIngredientClick)
checkChecked(); // this results in an initial flash, suck it up React
