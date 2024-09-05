package fr.outadoc.justchatting.feature.preferences.presentation.mobile

import android.content.Intent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainNavigation
import fr.outadoc.justchatting.feature.shared.presentation.mobile.Screen
import fr.outadoc.justchatting.shared.MR
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(
    modifier: Modifier = Modifier,
    onNavigate: (Screen) -> Unit,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

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

    MainNavigation(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        selectedScreen = Screen.Settings.Root,
        onSelectedTabChange = onNavigate,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(MR.strings.settings)) },
                scrollBehavior = scrollBehavior,
            )
        },
        content = { insets ->
            SettingsList(
                loggedInUser = state.user,
                onLogoutClick = viewModel::logout,
                onOpenDependencyCredits = { onNavigate(Screen.Settings.DependencyCredits) },
                onOpenThirdPartiesSection = { onNavigate(Screen.Settings.ThirdParties) },
                onOpenAboutSection = { onNavigate(Screen.Settings.About) },
                onOpenAppearanceSection = { onNavigate(Screen.Settings.Appearance) },
                onOpenNotificationSection = { onNavigate(Screen.Settings.Notifications) },
                insets = insets,
            )
        },
    )
}
