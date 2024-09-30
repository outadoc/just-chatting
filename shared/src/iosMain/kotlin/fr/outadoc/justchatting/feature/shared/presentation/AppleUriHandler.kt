package fr.outadoc.justchatting.feature.shared.presentation

import androidx.compose.ui.platform.UriHandler
import platform.Foundation.NSURL.Companion.URLWithString
import platform.UIKit.UIApplication

internal object AppleUriHandler : UriHandler {

    override fun openUri(uri: String) {
        UIApplication.sharedApplication.openURL(
            url = URLWithString(uri)!!,
            options = emptyMap<Any?, Any>(),
            completionHandler = null,
        )
    }
}
