package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.ui.platform.UriHandler
import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.deeplink.DeeplinkDefinitions

/**
 * Handles the app's own channel links in-app, and hands every other URI to [platformUriHandler].
 *
 * Not every platform routes `justchatting://` links back to the app: on desktop, opening one
 * through the system fails.
 */
internal class AppUriHandler(
    private val platformUriHandler: UriHandler,
    private val onDeeplinkReceived: (Uri) -> Unit,
) : UriHandler {
    override fun openUri(uri: String) {
        val parsed = Uri.parse(uri)
        if (parsed.isViewChannelUrl()) {
            onDeeplinkReceived(parsed)
        } else {
            platformUriHandler.openUri(uri)
        }
    }

    private fun Uri.isViewChannelUrl(): Boolean =
        scheme == DeeplinkDefinitions.ViewChannel.scheme &&
            host == DeeplinkDefinitions.ViewChannel.host
}
