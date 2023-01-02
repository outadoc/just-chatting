package fr.outadoc.justchatting.feature.preferences.presentation.mobile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    onOpenNotificationPreferences: () -> Unit,
    onOpenBubblePreferences: () -> Unit
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
        onLogoutClick = {
            viewModel.logout()
        },
        itemInsets = PaddingValues(horizontal = 16.dp)
    )
}