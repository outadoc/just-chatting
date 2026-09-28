package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import fr.outadoc.justchatting.feature.shared.presentation.Screen
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.channels
import fr.outadoc.justchatting.shared.internal.search
import fr.outadoc.justchatting.shared.internal.timeline_future
import fr.outadoc.justchatting.shared.internal.timeline_live
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Window width from which the navigation rail replaces the navigation bar.
 */
private const val NavigationRailMinWidthDp = 600

private data class NavigationDestination(
    val screen: Screen,
    val label: StringResource,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val destinations: List<NavigationDestination> =
    listOf(
        NavigationDestination(
            screen = Screen.Live,
            label = Res.string.timeline_live,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
        ),
        NavigationDestination(
            screen = Screen.Future,
            label = Res.string.timeline_future,
            selectedIcon = Icons.Filled.CalendarMonth,
            unselectedIcon = Icons.Outlined.CalendarMonth,
        ),
        NavigationDestination(
            screen = Screen.Followed,
            label = Res.string.channels,
            selectedIcon = Icons.Filled.Favorite,
            unselectedIcon = Icons.Outlined.FavoriteBorder,
        ),
        NavigationDestination(
            screen = Screen.Search,
            label = Res.string.search,
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search,
        ),
    )

/**
 * Wraps a main screen with the app's navigation: a navigation rail on wide windows, and a
 * navigation bar otherwise. Only the window's width is taken into account, so that short but
 * wide windows keep their navigation rail.
 */
@Composable
public fun MainNavigation(
    modifier: Modifier = Modifier,
    selectedScreen: Screen,
    onSelectedTabChange: (Screen) -> Unit,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val useNavigationRail: Boolean =
        currentWindowAdaptiveInfoV2()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(NavigationRailMinWidthDp)

    if (useNavigationRail) {
        Row(modifier = modifier.fillMaxSize()) {
            NavigationRail {
                // Center the items vertically
                Spacer(modifier = Modifier.weight(1f))

                destinations.forEach { destination ->
                    val isSelected = selectedScreen == destination.screen
                    NavigationRailItem(
                        selected = isSelected,
                        onClick = { onSelectedTabChange(destination.screen) },
                        icon = { DestinationIcon(destination, isSelected) },
                        label = { Text(stringResource(destination.label)) },
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        // The rail already handles the insets on its side of the screen
                        .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Start)),
            ) {
                Scaffold(
                    topBar = topBar,
                    content = content,
                )
            }
        }
    } else {
        Scaffold(
            modifier = modifier,
            topBar = topBar,
            bottomBar = {
                NavigationBar {
                    destinations.forEach { destination ->
                        val isSelected = selectedScreen == destination.screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { onSelectedTabChange(destination.screen) },
                            icon = { DestinationIcon(destination, isSelected) },
                            label = { Text(stringResource(destination.label)) },
                        )
                    }
                }
            },
            content = content,
        )
    }
}

@Composable
private fun DestinationIcon(
    destination: NavigationDestination,
    isSelected: Boolean,
) {
    Icon(
        imageVector =
            if (isSelected) {
                destination.selectedIcon
            } else {
                destination.unselectedIcon
            },
        contentDescription = null,
    )
}
