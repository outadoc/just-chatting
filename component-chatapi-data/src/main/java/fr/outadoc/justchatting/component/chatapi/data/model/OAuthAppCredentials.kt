package fr.outadoc.justchatting.component.chatapi.data.model

import android.net.Uri

data class OAuthAppCredentials(
    val clientId: String,
    val redirectUri: Uri
)
