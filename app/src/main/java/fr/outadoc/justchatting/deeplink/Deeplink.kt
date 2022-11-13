package fr.outadoc.justchatting.deeplink

sealed class Deeplink {
    data class ViewChannel(val login: String) : Deeplink()
    data class Authenticated(val token: String) : Deeplink()
}
