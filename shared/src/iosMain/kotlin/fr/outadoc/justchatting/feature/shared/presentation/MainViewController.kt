package fr.outadoc.justchatting.feature.shared.presentation

import platform.UIKit.UIViewController
import androidx.compose.ui.window.ComposeUIViewController
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
public fun getMainViewController(): UIViewController = ComposeUIViewController {
    val viewModel: MainRouterViewModel = koinViewModel()
    MainScreen(
        viewModel = viewModel,
        onChannelClick = {},
        onOpenNotificationPreferences = {},
        onOpenBubblePreferences = {},
        onOpenAccessibilityPreferences = {},
        onShareLogs = {},
        onOpenUri = {},
    )
}
