package fr.outadoc.justchatting.feature.preferences.presentation.mobile

import android.content.Intent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.home.presentation.mobile.MainNavigation
import fr.outadoc.justchatting.feature.home.presentation.mobile.Tab
import fr.outadoc.justchatting.feature.preferences.presentation.SettingsViewModel
import fr.outadoc.justchatting.shared.MR
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    sizeClass: WindowSizeClass,
    selectedTab: Tab,
    onSelectedTabChange: (Tab) -> Unit,
    onOpenNotificationPreferences: () -> Unit,
    onOpenBubblePreferences: () -> Unit,
    onOpenAccessibilityPreferences: () -> Unit,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val appPreferences by viewModel.appPreferences.collectAsState()

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
        sizeClass = sizeClass,
        selectedTab = selectedTab,
        onSelectedTabChange = onSelectedTabChange,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(MR.strings.settings)) },
                scrollBehavior = scrollBehavior,
            )
        },
        content = { insets ->
            SettingsList(
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
                insets = insets,
                versionName = context.applicationVersionName.orEmpty(),
            )
        },
    )
}
