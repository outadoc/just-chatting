package fr.outadoc.justchatting.feature.shared.presentation

import androidx.compose.ui.window.ComposeUIViewController
import com.eygraber.uri.toNSURL
import fr.outadoc.justchatting.feature.shared.presentation.mobile.App
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

public fun getMainViewController(): UIViewController =
    ComposeUIViewController {
        App(
            onOpenNotificationPreferences = {},
            onOpenBubblePreferences = {},
            onOpenAccessibilityPreferences = {},
            onShareLogs = {},
            onOpenUri = { uri ->
                uri.toNSURL()?.let { nsUrl ->
                    UIApplication.sharedApplication.openURL(nsUrl)
                }
            },
        )
    }
