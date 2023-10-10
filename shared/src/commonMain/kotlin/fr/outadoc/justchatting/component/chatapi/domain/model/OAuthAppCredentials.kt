package fr.outadoc.justchatting.component.chatapi.domain.model

import com.eygraber.uri.Uri

data class OAuthAppCredentials(
    val clientId: String,
    val redirectUri: Uri,
)
