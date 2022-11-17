package fr.outadoc.justchatting.oauth

import android.net.Uri

data class OAuthAppCredentials(
    val clientId: String,
    val redirectUri: Uri
)
