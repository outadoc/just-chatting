function getRedirectUri() {
  let redirect = new URL("justchatting://auth/callback");

  redirect.hash = window.location.hash;
  redirect.search = window.location.search;

  return redirect;
}

function redirect() {
  window.location = getRedirectUri();
}

redirect();
