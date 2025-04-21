package fr.outadoc.justchatting

import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.outadoc.justchatting.di.startSharedKoin
import fr.outadoc.justchatting.feature.shared.presentation.mobile.App
import fr.outadoc.justchatting.utils.logging.JvmLogStrategy
import fr.outadoc.justchatting.utils.logging.Logger

public fun startApp() {
    Logger.logStrategy = JvmLogStrategy
    startSharedKoin()
    application {
        Window(
            title = "Just Chatting",
            onCloseRequest = ::exitApplication,
        ) {
            val uriHandler = LocalUriHandler.current
            App(
                onOpenNotificationPreferences = {},
                onOpenBubblePreferences = {},
                onOpenAccessibilityPreferences = {},
                onShareLogs = {},
                onShowAuthPage = { uri ->
                    uriHandler.openUri(uri.toString())
                },
            )
        }
    }
}
