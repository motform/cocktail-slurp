// mobile filter menu toggle
let toggle = false;

document.getElementById("ftoggle").onclick = () => {
  document.getElementById("strainer").style.display = (toggle ? "none" : "flex");
  document.getElementById("cards").style.display    = (toggle ? "grid" : "none");
  toggle = !toggle;
}

// auto-filtering ingredients

let selectedIngredients = new Set();
let possibleIngredients = new Set();
let HTTPRequest;

const rest = ([car, ...cdr]) => cdr;

const ingredientLabels =
      Array.from(document.getElementsByClassName("ingredients"))
      .map(is => rest(Array.from(is.children)))
      .flat()
      .filter(i => i.type !== "checkbox");

function checkIngredients() {
  if (HTTPRequest.readyState === XMLHttpRequest.DONE) {
    possibleIngredients = new Set(JSON.parse(HTTPRequest.response));
    if (possibleIngredients.size)
      ingredientLabels.map(i => i.style.display = (possibleIngredients.has(i.textContent) ? "block" : "none"));
    else
      ingredientLabels.map(i => i.style.display = "block");
  }
}

function requestIngredientCheck() {
  let params = new URLSearchParams(); // this could be a json array, but I like my query strings
  Array.from(selectedIngredients).map(i => params.append("ingredient", i));

  HTTPRequest = new XMLHttpRequest();
  HTTPRequest.onreadystatechange = checkIngredients;
  HTTPRequest.open("GET", "/possible-ingredients" + "?" + params.toString(), true);
  HTTPRequest.send();
}

function onIngredientClick(ingredient) {
  const ingredientName = ingredient.target.textContent;
  selectedIngredients.has(ingredientName)
    ? selectedIngredients.delete(ingredientName)
    : selectedIngredients.add(ingredientName);
  requestIngredientCheck()
}

ingredientLabels.map(i => i.onclick = onIngredientClick)
