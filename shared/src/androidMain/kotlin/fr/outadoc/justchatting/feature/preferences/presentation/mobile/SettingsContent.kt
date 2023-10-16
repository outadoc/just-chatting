package fr.outadoc.justchatting.feature.preferences.presentation.mobile

import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    onOpenNotificationPreferences: () -> Unit,
    onOpenBubblePreferences: () -> Unit,
    onOpenAccessibilityPreferences: () -> Unit,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val appPreferences by viewModel.appPreferences.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsViewModel.Event.ShareLogs -> {
                    val sendIntent: Intent =
                        Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, event.uri.toUri())
                            type = "application/gzip"
                        }

                    context.startActivity(
                        Intent.createChooser(sendIntent, null),
                    )
                }
            }
        }
    }

    SettingsList(
        modifier = modifier,
        appPreferences = appPreferences,
        onAppPreferencesChange = { updated ->
            viewModel.updatePreferences(updated)
        },
        onOpenNotificationPreferences = onOpenNotificationPreferences,
        onOpenBubblePreferences = onOpenBubblePreferences,
        onOpenAccessibilityPreferences = onOpenAccessibilityPreferences,
        onLogoutClick = viewModel::logout,
        onShareLogsClick = viewModel::onShareLogsClick,
        readDependencies = koinInject(),
        itemInsets = PaddingValues(horizontal = 16.dp),
        versionName = context.applicationVersionName.orEmpty(),
    )
}
