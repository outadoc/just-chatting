package fr.outadoc.justchatting.feature.deeplink

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials

internal data class DeeplinkParser(
    private val oAuthAppCredentials: OAuthAppCredentials,
) {
    fun parseDeeplink(uri: Uri): Deeplink? {
        when {
            uri.isViewChannelUrl() -> {
                uri.pathSegments.firstOrNull()?.let { userId ->
                    return Deeplink.ViewChannel(userId = userId)
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

    private fun Uri.isViewChannelUrl(): Boolean = scheme == DeeplinkDefinitions.ViewChannel.scheme &&
        host == DeeplinkDefinitions.ViewChannel.host

    private fun Uri.isRedirectUrl(): Boolean {
        val redirectUri = Uri.parse(oAuthAppCredentials.redirectUri)
        val isFromUniversalLink =
            scheme == redirectUri.scheme &&
                host == redirectUri.host &&
                path == redirectUri.path

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
