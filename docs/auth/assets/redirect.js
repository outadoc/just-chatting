function appCallbackUrl() {
  const url = new URL("justchatting://auth/callback");

  url.hash = window.location.hash;
  url.search = window.location.search;

  return url;
}

function redirect() {
  window.location = appCallbackUrl();
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

// If the automatic redirect doesn't go through, the button opens the app with the same token.
document.getElementById("open-app").href = appCallbackUrl().toString();

callLocalServer();
redirect();
