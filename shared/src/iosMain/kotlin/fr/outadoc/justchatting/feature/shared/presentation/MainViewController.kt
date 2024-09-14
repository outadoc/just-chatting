package fr.outadoc.justchatting.feature.shared.presentation

import androidx.compose.ui.window.ComposeUIViewController
import com.eygraber.uri.toNSURL
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

@OptIn(KoinExperimentalAPI::class)
public fun getMainViewController(): UIViewController =
    ComposeUIViewController {
        val viewModel: MainRouterViewModel = koinViewModel()
        MainScreen(
            viewModel = viewModel,
            onChannelClick = {},
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
