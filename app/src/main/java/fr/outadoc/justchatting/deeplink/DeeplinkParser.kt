package fr.outadoc.justchatting.deeplink

import android.net.Uri
import androidx.core.net.toUri
import fr.outadoc.justchatting.repository.PreferenceRepository
import fr.outadoc.justchatting.util.viewChannelBaseUrl
import kotlinx.coroutines.flow.firstOrNull

data class DeeplinkParser(
    val preferencesRepository: PreferenceRepository
) {
    suspend fun parseDeeplink(uri: Uri): Deeplink? {
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

    private suspend fun Uri.isRedirectUrl(): Boolean {
        val redirectUri: Uri? =
            preferencesRepository.currentPreferences
                .firstOrNull()
                ?.helixRedirect
                ?.toUri()

        return redirectUri != null &&
                scheme == redirectUri.scheme &&
                host == redirectUri.host &&
                path == redirectUri.path
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
