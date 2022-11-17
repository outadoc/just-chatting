package fr.outadoc.justchatting.deeplink

import android.net.Uri
import fr.outadoc.justchatting.oauth.OAuthAppCredentials
import fr.outadoc.justchatting.util.viewChannelBaseUrl

data class DeeplinkParser(
    private val oAuthAppCredentials: OAuthAppCredentials
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
        return scheme == viewChannelBaseUrl.scheme && host == viewChannelBaseUrl.host
    }

    private fun Uri.isRedirectUrl(): Boolean {
        return scheme == oAuthAppCredentials.redirectUri.scheme &&
            host == oAuthAppCredentials.redirectUri.host &&
            path == oAuthAppCredentials.redirectUri.path
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
