package fr.outadoc.justchatting.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.getViewModel

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    onOpenNotificationPreferences: () -> Unit,
    onOpenBubblePreferences: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val viewModel: SettingsViewModel = getViewModel()
    val appPreferences by viewModel.appPreferences.collectAsState()

    SettingsList(
        modifier = modifier,
        appPreferences = appPreferences,
        onAppPreferencesChange = { updated ->
            viewModel.updatePreferences(updated)
        },
        onOpenNotificationPreferences = onOpenNotificationPreferences,
        onOpenBubblePreferences = onOpenBubblePreferences,
        onLogoutClick = onLogoutClick,
        itemInsets = PaddingValues(horizontal = 16.dp)
    )
}
