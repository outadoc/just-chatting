package fr.outadoc.justchatting.feature.deeplink

internal sealed class Deeplink {
    data class ViewChannel(val login: String) : Deeplink()
    data class Authenticated(val token: String) : Deeplink()
}
