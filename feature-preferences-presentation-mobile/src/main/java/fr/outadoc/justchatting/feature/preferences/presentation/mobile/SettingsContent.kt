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
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    onOpenNotificationPreferences: () -> Unit,
    onOpenBubblePreferences: () -> Unit,
) {
    val viewModel: SettingsViewModel = getViewModel()
    val appPreferences by viewModel.appPreferences.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsViewModel.Event.ShareLogs -> {
                    val sendIntent: Intent =
                        Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, event.uri)
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
        onLogoutClick = viewModel::logout,
        onShareLogsClick = viewModel::onShareLogsClick,
        itemInsets = PaddingValues(horizontal = 16.dp),
    )
}
