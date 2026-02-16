package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.channels
import fr.outadoc.justchatting.shared.search
import fr.outadoc.justchatting.shared.settings
import fr.outadoc.justchatting.shared.timeline_future
import fr.outadoc.justchatting.shared.timeline_live
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MainNavigation(
    modifier: Modifier = Modifier,
    selectedScreen: Screen,
    onSelectedTabChange: (Screen) -> Unit,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val navSuiteType: NavigationSuiteType =
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
            currentWindowAdaptiveInfo(),
        )

    NavigationSuiteScaffold(
        modifier = modifier,
        layoutType = navSuiteType,
        navigationSuiteItems = {
            item(
                selected = selectedScreen == Screen.Live,
                onClick = { onSelectedTabChange(Screen.Live) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(Res.string.timeline_live)) },
            )

            item(
                selected = selectedScreen == Screen.Future,
                onClick = { onSelectedTabChange(Screen.Future) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Upcoming,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(Res.string.timeline_future)) },
            )

            item(
                selected = selectedScreen == Screen.Followed,
                onClick = { onSelectedTabChange(Screen.Followed) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(Res.string.channels)) },
            )

            item(
                selected = selectedScreen == Screen.Search,
                onClick = { onSelectedTabChange(Screen.Search) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(Res.string.search)) },
            )

            item(
                selected = selectedScreen == Screen.Settings,
                onClick = { onSelectedTabChange(Screen.Settings) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(Res.string.settings)) },
            )
        },
        content = {
            // Workaround for iPad-specific status bar padding issue
            // Remove if it ever gets fixed in Compose
            Scaffold(
                topBar = topBar,
                content = content,
                contentWindowInsets =
                when (navSuiteType) {
                    NavigationSuiteType.NavigationBar -> WindowInsets.statusBars
                    else -> ScaffoldDefaults.contentWindowInsets
                },
            )
        },
    )
}
