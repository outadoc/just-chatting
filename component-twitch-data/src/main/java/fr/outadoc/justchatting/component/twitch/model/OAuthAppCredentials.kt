package fr.outadoc.justchatting.component.twitch.model

import android.net.Uri

data class OAuthAppCredentials(
    val clientId: String,
    val redirectUri: Uri
)