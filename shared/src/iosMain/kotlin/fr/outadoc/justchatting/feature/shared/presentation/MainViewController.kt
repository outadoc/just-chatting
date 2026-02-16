package fr.outadoc.justchatting.feature.shared.presentation

import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.window.ComposeUIViewController
import com.eygraber.uri.toNSURL
import fr.outadoc.justchatting.feature.shared.presentation.ui.App
import platform.Foundation.NSURL
import platform.UIKit.UIViewController

public fun getMainViewController(onShowAuthPage: (NSURL) -> Unit): UIViewController = ComposeUIViewController {
    val uriHandler = LocalUriHandler.current
    App(
        onOpenAccessibilityPreferences = {
            uriHandler.openUri("app-settings:")
        },
        onOpenNotificationPreferences = {},
        onOpenBubblePreferences = {},
        onShareLogs = {},
        onShowAuthPage = { uri ->
            uri.toNSURL()?.let(onShowAuthPage)
        },
    )
}
