package fr.outadoc.justchatting.feature.preferences.presentation.mobile

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
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainNavigation
import fr.outadoc.justchatting.feature.shared.presentation.mobile.Screen
import fr.outadoc.justchatting.shared.MR
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
internal fun SettingsContent(
    modifier: Modifier = Modifier,
    onNavigate: (Screen) -> Unit,
    onShareLogs: (uri: String) -> Unit,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsViewModel.Event.ShareLogs -> {
                    onShareLogs(event.uri)
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