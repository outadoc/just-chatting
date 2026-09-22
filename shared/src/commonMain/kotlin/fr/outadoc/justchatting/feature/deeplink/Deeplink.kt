package fr.outadoc.justchatting.feature.deeplink

import fr.outadoc.justchatting.feature.preferences.domain.model.ApiToken

public sealed class Deeplink {
    public data class ViewChannel(
        val userId: String,
    ) : Deeplink()

    public data class Authenticated(
        val token: ApiToken,
    ) : Deeplink()
}
