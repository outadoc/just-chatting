package fr.outadoc.justchatting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.outadoc.justchatting.di.startSharedKoin
import fr.outadoc.justchatting.feature.chat.presentation.ui.SlimSnackbar
import fr.outadoc.justchatting.feature.preferences.presentation.AppUpdateChecker
import fr.outadoc.justchatting.feature.preferences.presentation.ui.UpdateAvailableDialog
import fr.outadoc.justchatting.feature.shared.presentation.ui.App
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.app_name
import fr.outadoc.justchatting.shared.internal.icon_masked
import fr.outadoc.justchatting.shared.internal.settings_about_update_subtitle_available
import fr.outadoc.justchatting.utils.logging.JvmLogStrategy
import fr.outadoc.justchatting.utils.logging.Logger
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

public fun startApp() {
    Logger.logStrategy = JvmLogStrategy
    startSharedKoin()

    application {
        Window(
            title = stringResource(Res.string.app_name),
            onCloseRequest = ::exitApplication,
            icon = painterResource(Res.drawable.icon_masked),
        ) {
            val uriHandler = LocalUriHandler.current
            val appUpdateChecker: AppUpdateChecker = koinInject()
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                if (appUpdateChecker.isSupported) {
                    appUpdateChecker.checkForUpdate()
                }
            }

            val updateState by appUpdateChecker.state.collectAsState()
            var showUpdateDialog by remember { mutableStateOf(false) }

            Box {
                App(
                    onOpenNotificationPreferences = {},
                    onOpenBubblePreferences = {},
                    onOpenAccessibilityPreferences = {},
                    onShareLogs = {},
                    onShowAuthPage = { uri ->
                        uriHandler.openUri(uri.toString())
                    },
                )

                val availableVersion = updateState.availableVersion
                if (availableVersion != null) {
                    SlimSnackbar(
                        modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .clickable { showUpdateDialog = true },
                    ) {
                        Text(
                            text =
                            stringResource(
                                Res.string.settings_about_update_subtitle_available,
                                availableVersion,
                            ),
                        )
                    }
                }
            }

            if (showUpdateDialog) {
                UpdateAvailableDialog(
                    state = updateState,
                    onConfirm = {
                        coroutineScope.launch {
                            appUpdateChecker.installUpdate()
                        }
                    },
                    onDismiss = { showUpdateDialog = false },
                )
            }
        }
    }
}
