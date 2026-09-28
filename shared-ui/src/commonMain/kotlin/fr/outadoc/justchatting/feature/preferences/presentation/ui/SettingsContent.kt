package fr.outadoc.justchatting.feature.preferences.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import fr.outadoc.justchatting.feature.shared.presentation.DetailScreen
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.all_goBack
import fr.outadoc.justchatting.shared.internal.settings
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onNavigateDetails: (DetailScreen) -> Unit,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsViewModel.Event.NavigateToDetail -> {
                    onNavigateDetails(event.screen)
                }

                is SettingsViewModel.Event.ShareLogs,
                is SettingsViewModel.Event.CopyLogsToClipboard,
                -> {
                    Unit
                } // Handled in SettingsSectionAbout, next to the triggering button.
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings)) },
                navigationIcon = {
                    AccessibleIconButton(
                        onClick = onNavigateUp,
                        onClickLabel = stringResource(Res.string.all_goBack),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        content = { insets ->
            SettingsList(
                loggedInUser = state.user,
                appVersionName = state.appVersionName,
                onLogoutClick = viewModel::logout,
                onOpenDependencyCredits = { viewModel.onNavigateToDetail(DetailScreen.DependencyCredits) },
                onOpenThirdPartiesSection = { viewModel.onNavigateToDetail(DetailScreen.ThirdParties) },
                onOpenAboutSection = { viewModel.onNavigateToDetail(DetailScreen.About) },
                onOpenAppearanceSection = { viewModel.onNavigateToDetail(DetailScreen.Appearance) },
                onOpenNotificationSection = { viewModel.onNavigateToDetail(DetailScreen.Notifications) },
                insets = insets,
            )
        },
    )
}
