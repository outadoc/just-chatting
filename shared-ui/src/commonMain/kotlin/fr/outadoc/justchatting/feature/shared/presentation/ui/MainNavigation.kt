package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.feature.shared.presentation.Screen
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.channels
import fr.outadoc.justchatting.shared.internal.search
import fr.outadoc.justchatting.shared.internal.timeline_future
import fr.outadoc.justchatting.shared.internal.timeline_live
import org.jetbrains.compose.resources.stringResource

@Composable
public fun MainNavigation(
    modifier: Modifier = Modifier,
    selectedScreen: Screen,
    onSelectedTabChange: (Screen) -> Unit,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val navSuiteType: NavigationSuiteType =
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
            currentWindowAdaptiveInfoV2(),
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
                        imageVector =
                            if (selectedScreen == Screen.Live) {
                                Icons.Filled.Home
                            } else {
                                Icons.Outlined.Home
                            },
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
                        imageVector =
                            if (selectedScreen == Screen.Future) {
                                Icons.Filled.CalendarMonth
                            } else {
                                Icons.Outlined.CalendarMonth
                            },
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
                        imageVector =
                            if (selectedScreen == Screen.Followed) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
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
                        imageVector =
                            if (selectedScreen == Screen.Search) {
                                Icons.Filled.Search
                            } else {
                                Icons.Outlined.Search
                            },
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(Res.string.search)) },
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
