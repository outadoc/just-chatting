package fr.outadoc.justchatting

import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.outadoc.justchatting.di.startSharedKoin
import fr.outadoc.justchatting.feature.shared.presentation.ui.App
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.app_name
import fr.outadoc.justchatting.shared.icon_masked
import fr.outadoc.justchatting.utils.logging.JvmLogStrategy
import fr.outadoc.justchatting.utils.logging.Logger
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

public fun startApp() {
    Logger.logStrategy = JvmLogStrategy
    startSharedKoin()

    application {
        Window(
            title = stringResource(Res.string.app_name),
            onCloseRequest = ::exitApplication,
            icon = painterResource(Res.drawable.icon_masked),
        ) {
            WithScaling {
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
}
