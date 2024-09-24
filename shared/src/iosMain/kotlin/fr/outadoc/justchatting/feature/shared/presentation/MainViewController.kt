package fr.outadoc.justchatting.feature.shared.presentation

import androidx.compose.ui.window.ComposeUIViewController
import com.eygraber.uri.toNSURL
import fr.outadoc.justchatting.feature.shared.presentation.mobile.App
import platform.Foundation.NSURL
import platform.UIKit.UIViewController

public fun getMainViewController(
    onShowAuthPage: (NSURL) -> Unit,
    onOpenAccessibilityPreferences: () -> Unit,
): UIViewController =
    ComposeUIViewController {
        App(
            onOpenAccessibilityPreferences = onOpenAccessibilityPreferences,
            onOpenNotificationPreferences = {},
            onOpenBubblePreferences = {},
            onShareLogs = {},
            onShowAuthPage = { uri -> uri.toNSURL()?.let(onShowAuthPage) },
        )
    }
