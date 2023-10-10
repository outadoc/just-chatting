package fr.outadoc.justchatting.component.deeplink

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.component.chatapi.domain.model.OAuthAppCredentials

data class DeeplinkParser(
    private val oAuthAppCredentials: OAuthAppCredentials,
) {
    fun parseDeeplink(uri: Uri): Deeplink? {
        when {
            uri.isViewChannelUrl() -> {
                uri.pathSegments.firstOrNull()?.let { login ->
                    return Deeplink.ViewChannel(login = login)
                }
            }

            uri.isRedirectUrl() -> {
                val token = uri.parseToken()
                if (token != null) {
                    return Deeplink.Authenticated(token = token)
                }
            }
        }

        return null
    }

    private fun Uri.isViewChannelUrl(): Boolean {
        return scheme == DeeplinkDefinitions.ViewChannel.scheme &&
            host == DeeplinkDefinitions.ViewChannel.host
    }

    private fun Uri.isRedirectUrl(): Boolean {
        val isFromUniversalLink =
            scheme == oAuthAppCredentials.redirectUri.scheme &&
                host == oAuthAppCredentials.redirectUri.host &&
                path == oAuthAppCredentials.redirectUri.path

        val isFromDeeplink =
            scheme == DeeplinkDefinitions.AuthCallback.scheme &&
                host == DeeplinkDefinitions.AuthCallback.host &&
                path == DeeplinkDefinitions.AuthCallback.path

        return isFromDeeplink || isFromUniversalLink
    }

    private fun Uri.parseToken(): String? {
        // URL contains query parameters encoded as a path fragment.
        // Copy the path fragment to query parameters and parse them this way.
        return buildUpon()
            .encodedQuery(fragment)
            .build()
            .getQueryParameter("access_token")
    }
}
