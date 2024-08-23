package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.shared.MR

@Composable
internal fun MainNavigation(
    modifier: Modifier = Modifier,
    sizeClass: WindowSizeClass,
    selectedScreen: Screen,
    onSelectedTabChange: (Screen) -> Unit,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    when (sizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            CompactNavigation(
                modifier = modifier,
                selectedScreen = selectedScreen,
                onSelectedTabChange = onSelectedTabChange,
                topBar = topBar,
                content = content,
            )
        }

        WindowWidthSizeClass.Medium -> {
            MediumNavigation(
                modifier = modifier,
                selectedScreen = selectedScreen,
                onSelectedTabChange = onSelectedTabChange,
                topBar = topBar,
                content = content,
            )
        }

        WindowWidthSizeClass.Expanded -> {
            ExpandedNavigation(
                modifier = modifier,
                selectedScreen = selectedScreen,
                onSelectedTabChange = onSelectedTabChange,
                topBar = topBar,
                content = content,
            )
        }
    }
}

@Composable
internal fun CompactNavigation(
    modifier: Modifier = Modifier,
    selectedScreen: Screen,
    onSelectedTabChange: (Screen) -> Unit,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedScreen == Screen.Epg,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                        )
                    },
                    onClick = { onSelectedTabChange(Screen.Epg) },
                )

                NavigationBarItem(
                    selected = selectedScreen == Screen.Followed,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                        )
                    },
                    onClick = { onSelectedTabChange(Screen.Followed) },
                )

                NavigationBarItem(
                    selected = selectedScreen == Screen.Search,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                        )
                    },
                    onClick = { onSelectedTabChange(Screen.Search) },
                )

                NavigationBarItem(
                    selected = selectedScreen == Screen.Settings.Root,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                        )
                    },
                    onClick = { onSelectedTabChange(Screen.Settings.Root) },
                )
            }
        },
        content = content,
    )
}

@Composable
internal fun MediumNavigation(
    modifier: Modifier = Modifier,
    selectedScreen: Screen,
    onSelectedTabChange: (Screen) -> Unit,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Row {
        NavigationRail {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(
                    16.dp,
                    alignment = Alignment.CenterVertically,
                ),
            ) {
                NavigationRailItem(
                    selected = selectedScreen == Screen.Epg,
                    label = {
                        AnimatedVisibility(visible = selectedScreen == Screen.Followed) {
                            Text(stringResource(MR.strings.epg_title))
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                        )
                    },
                    onClick = { onSelectedTabChange(Screen.Epg) },
                )

                NavigationRailItem(
                    selected = selectedScreen == Screen.Followed,
                    label = {
                        AnimatedVisibility(visible = selectedScreen == Screen.Followed) {
                            Text(stringResource(MR.strings.channels))
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                        )
                    },
                    onClick = { onSelectedTabChange(Screen.Followed) },
                )

                NavigationRailItem(
                    selected = selectedScreen == Screen.Search,
                    label = {
                        AnimatedVisibility(visible = selectedScreen == Screen.Search) {
                            Text(stringResource(MR.strings.search))
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                        )
                    },
                    onClick = { onSelectedTabChange(Screen.Search) },
                )

                NavigationRailItem(
                    selected = selectedScreen == Screen.Settings.Root,
                    label = {
                        AnimatedVisibility(visible = selectedScreen == Screen.Settings.Root) {
                            Text(stringResource(MR.strings.settings))
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                        )
                    },
                    onClick = { onSelectedTabChange(Screen.Settings.Root) },
                )
            }
        }

        Scaffold(
            modifier = modifier,
            topBar = topBar,
            content = content,
        )
    }
}

@Composable
internal fun ExpandedNavigation(
    modifier: Modifier = Modifier,
    selectedScreen: Screen,
    onSelectedTabChange: (Screen) -> Unit,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    PermanentNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            PermanentDrawerSheet(
                modifier = Modifier
                    .width(240.dp)
                    .padding(
                        vertical = 20.dp,
                        horizontal = 8.dp,
                    )
                    .systemBarsPadding(),
            ) {
                NavigationDrawerItem(
                    modifier = Modifier.padding(4.dp),
                    selected = selectedScreen == Screen.Epg,
                    label = { Text(stringResource(MR.strings.epg_title)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                        )
                    },
                    onClick = { onSelectedTabChange(Screen.Epg) },
                )

                NavigationDrawerItem(
                    modifier = Modifier.padding(4.dp),
                    selected = selectedScreen == Screen.Followed,
                    label = { Text(stringResource(MR.strings.channels)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                        )
                    },
                    onClick = { onSelectedTabChange(Screen.Followed) },
                )

                NavigationDrawerItem(
                    modifier = Modifier.padding(4.dp),
                    selected = selectedScreen == Screen.Search,
                    label = { Text(stringResource(MR.strings.search)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                        )
                    },
                    onClick = { onSelectedTabChange(Screen.Search) },
                )

                NavigationDrawerItem(
                    modifier = Modifier.padding(4.dp),
                    selected = selectedScreen == Screen.Settings.Root,
                    label = { Text(stringResource(MR.strings.settings)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                        )
                    },
                    onClick = { onSelectedTabChange(Screen.Settings.Root) },
                )
            }
        },
        content = {
            Scaffold(
                modifier = modifier,
                topBar = topBar,
                content = content,
            )
        },
    )
}
