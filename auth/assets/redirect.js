function redirect() {
  let redirect = new URL("justchatting://auth/callback");

  redirect.hash = window.location.hash;
  redirect.search = window.location.search;

  window.location = redirect;
}

function callLocalServer() {
  const localServer = new XMLHttpRequest();
  const uri = "http://localhost:3000/auth/callback" + window.location.search + window.location.hash;

  localServer.open("GET", uri, true);
  localServer.onreadystatechange = function () {
    if (localServer.readyState === 4 && localServer.status === 200) {
      console.log("Local server called successfully.");
    } else if (localServer.readyState === 4) {
      console.error("Failed to call local server.");
    }
  };

  localServer.send();
}

callLocalServer();
redirect();
