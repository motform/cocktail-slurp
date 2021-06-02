toggle = false;

document.getElementById("ftoggle").onclick = () => {
  document.getElementById("strainer").style.display = (toggle ? "none" : "flex");
  document.getElementById("cards").style.display = (toggle ? "grid" : "none");
  toggle = (toggle ? false : true);
}

