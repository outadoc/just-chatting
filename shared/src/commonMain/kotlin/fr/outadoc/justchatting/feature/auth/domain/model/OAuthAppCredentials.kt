package fr.outadoc.justchatting.feature.auth.domain.model

internal data class OAuthAppCredentials(
    val clientId: String,
    val redirectUri: String,
)
