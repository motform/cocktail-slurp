// mobile filter menu toggle
let toggle = false;

document.getElementById("ftoggle").onclick = () => {
  document.getElementById("strainer").style.display = (toggle ? "none" : "flex");
  document.getElementById("cards").style.display    = (toggle ? "grid" : "none");
  toggle = !toggle;
}

// auto-filtering ingredients

const rest = ([car, ...cdr]) => cdr;

const isActiveSection = ([ingredientSection]) =>
      ingredientSection.filter(ingredientContainer => {
        const [_, label] = Array.from(ingredientContainer.children);
        return possibleIngredients[label.htmlFor];
      }).length;

function checkIngredientSections() {
  for (const [category, section] of ingredientSections) {
    category.style.display = (isActiveSection([section]) ? "flex" :  "none");
  }
}

function checkIngredients() {
  if (HTTPRequest.readyState === XMLHttpRequest.DONE) {
    possibleIngredients = JSON.parse(HTTPRequest.response);

    for (ingredientContainer of ingredientContainers) {
      const [_, label, count] = Array.from(ingredientContainer.children);
      ingredientName = label.htmlFor;
      const possibleCocktails = possibleIngredients[ingredientName];
      const isSelected = selectedIngredients.has(ingredientName);

      ingredientContainer.style.display = (possibleCocktails ? "flex" : "none");
      // label.style.color = (isSelected ? "var(--fg1)" : "var(--fg3)");
      count.innerText = (possibleCocktails ? possibleCocktails : "");
    }

    checkIngredientSections();
  }
}

function requestIngredientCheck() {
  if (selectedIngredients.size) {
    let params = new URLSearchParams(); // this could be a json array, but I like my query strings
    for (ingredient of Array.from(selectedIngredients)) {
      params.append("ingredient", ingredient);
    }

    HTTPRequest = new XMLHttpRequest();
    HTTPRequest.onreadystatechange = checkIngredients;
    HTTPRequest.open("GET", "/possible-ingredients" + "?" + params.toString(), true);
    HTTPRequest.send();
  } else {
    for (ingredientContainer of ingredientContainers) {
      const [_, label, count] = Array.from(ingredientContainer.children)
      ingredientContainer.style.display = "flex";
      count.innerText = "";
    }
  }
}

function onIngredientClick(ingredientContainer) {
  const [checkbox, label, count] = Array.from(ingredientContainer.children);
  const ingredientName = label.htmlFor;

  return function() {
    if (selectedIngredients.has(ingredientName)) {
      selectedIngredients.delete(ingredientName);
      checkbox.checked = false;
    } else {
      selectedIngredients.add(ingredientName);
      checkbox.checked = true;
    }

    requestIngredientCheck()
  }
}

function checkChecked() {
  for (ingredientContainer of ingredientContainers) {
    const [checkbox, label, _] = Array.from(ingredientContainer.children);
    if (checkbox.checked) selectedIngredients.add(label.htmlFor)
    requestIngredientCheck();
  }
}

let selectedIngredients = new Set();
let possibleIngredients = {};
let HTTPRequest;

let ingredientSections = Array.from(document.getElementsByClassName("ingredients"));
const ingredientContainers = ingredientSections.map(is => rest(Array.from(is.children))).flat();

ingredientSections = ingredientSections.map(is => [is, rest(Array.from(is.children))]);  // an array of [section, sectionIngredients]

ingredientContainers.map(i => i.onclick = onIngredientClick(i));
checkChecked(); // this results in an initial flash, suck it up React
