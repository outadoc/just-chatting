package fr.outadoc.justchatting.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.R
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBackPress: () -> Unit = {},
    onOpenNotificationPreferences: () -> Unit,
    onOpenBubblePreferences: () -> Unit
) {
    val viewModel: SettingsViewModel = getViewModel()
    val appPreferences by viewModel.appPreferences.collectAsState()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.all_goBack)
                        )
                    }
                },
                title = {
                    Text(stringResource(R.string.settings))
                }
            )
        }
    ) { insets ->
        SettingsList(
            appPreferences = appPreferences,
            onAppPreferencesChange = { appPreferences ->
                viewModel.updatePreferences(appPreferences)
            },
            onOpenNotificationPreferences = onOpenNotificationPreferences,
            onOpenBubblePreferences = onOpenBubblePreferences,
            itemInsets = PaddingValues(horizontal = 16.dp),
            insets = insets
        )
    }
}
