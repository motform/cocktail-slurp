"use strict";

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
        const [, label] = Array.from(ingredientContainer.children);
        return possibleIngredients[label.htmlFor];
      }).length;

function checkIngredientSections() {
  for (const [category, section] of ingredientSections) {
    category.style.display = (isActiveSection([section]) ? "flex" :  "none");
  }
}

function checkIngredients() {
  if (HTTPRequest.readyState === XMLHttpRequest.DONE) {
    const response = JSON.parse(HTTPRequest.response);
    possibleIngredients = response.ingredients;
    const cocktailCards = response.cocktailCards;

    // Flush and update the cocktail cards.
    if (pageLoad) {
      pageLoad = false; // Don't do it the first time!
    } else {
      cocktailPage.removeChild(cocktailPage.lastChild);
      cocktailPage.insertAdjacentHTML("beforeend", cocktailCards);
      window.scrollTo(0, 0);
    }

    // Update the state of the strainer.
    for (const ingredientContainer of ingredientContainers) {
      const [, label, count] = Array.from(ingredientContainer.children);
      const ingredientName = label.htmlFor;
      const possibleCocktails = possibleIngredients[ingredientName];
      const isSelected = selectedIngredients.has(ingredientName);

      ingredientContainer.style.display = (possibleCocktails ? "flex" : "none");
      ingredientContainer.style.backgroundColor = (isSelected ? "var(--beige8)" : "");
      count.innerText = (possibleCocktails ? possibleCocktails : "");
    }

    checkIngredientSections();
  }
}

function requestIngredientCheck() {
  if (selectedIngredients.size || pageLoad || true) {
    let params = new URLSearchParams(); // this could be a json array, but I like my query strings
    for (const ingredient of Array.from(selectedIngredients)) {
      params.append("ingredient", ingredient);
    }

    if (params.toString().length) {
      history.pushState({}, "", "/cocktails" + "?" + params.toString());
    }

    HTTPRequest = new XMLHttpRequest();
    HTTPRequest.onreadystatechange = checkIngredients;
    HTTPRequest.open("GET", "/api/possible-ingredients" + "?" + params.toString(), true);
    HTTPRequest.send();

  } else { // No ingredients are selected, reset the state of the strainer.
    for (const ingredientContainer of ingredientContainers) {
      const [, label, count] = Array.from(ingredientContainer.children);
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
      ingredientContainer.style.backgroundColor = "";
      checkbox.checked = false;
    } else {
      selectedIngredients.add(ingredientName);
      checkbox.checked = true;
    }

    requestIngredientCheck()
  }
}

function checkChecked() {
  for (const ingredientContainer of ingredientContainers) {
    const [checkbox, label,] = Array.from(ingredientContainer.children);
    if (checkbox.checked) {
      selectedIngredients.add(label.htmlFor)
    }
  }
  requestIngredientCheck();
}

let selectedIngredients = new Set();
let possibleIngredients = {};
let HTTPRequest;

let ingredientSections = Array.from(document.getElementsByClassName("ingredients"));
const ingredientContainers = ingredientSections.map(is => rest(Array.from(is.children))).flat();

ingredientSections = ingredientSections.map(is => [is, rest(Array.from(is.children))]);  // an array of [section, sectionIngredients]

ingredientContainers.map(i => i.onclick = onIngredientClick(i));
let pageLoad = true;
checkChecked(); // this results in an initial flash, suck it up React

const cocktailPage = document.querySelector("main");

/*
  TODO:

  1. turn the filter-cocktails button into a clear-filters button on desktop?
  2. stop extranious cocktail-requests on mobile (where the views are hidden anyway)
  3. make the cocktail-page look less boring
  4. add "similar cocktails" to the cocktail-page

 */
