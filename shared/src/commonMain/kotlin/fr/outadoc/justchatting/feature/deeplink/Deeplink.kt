package fr.outadoc.justchatting.feature.deeplink

public sealed class Deeplink {
    public data class ViewChannel(
        val userId: String,
    ) : Deeplink()

    public data class Authenticated(
        val token: String,
    ) : Deeplink()
}
