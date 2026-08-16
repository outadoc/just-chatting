function redirect() {
  let redirect = new URL("justchatting://auth/callback");

  redirect.hash = window.location.hash;
  redirect.search = window.location.search;

  window.location = redirect;
}

function callLocalServer() {
  // Take the URI fragment (hash) and convert it to a query string
  const hash = window.location.hash.substring(1);
  const params = new URLSearchParams(hash);
  const queryString = params.toString();
  const url = new URL("http://localhost:45563/auth/callback");
  url.search = queryString;

  // Make a GET request to the local server with the query string
  fetch(url, {
    method: "GET",
  })
    .then((response) => response.text())
    .then((data) => {
      console.log("Success:", data);
    })
    .catch((error) => {
      console.error("Error:", error);
    });
}

callLocalServer();
redirect();
