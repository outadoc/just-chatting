package fr.outadoc.justchatting.ui.main

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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.ui.search.channels.ChannelSearchViewModel

@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    sizeClass: WindowSizeClass,
    selectedTab: Tab,
    onSelectedTabChange: (Tab) -> Unit,
    searchViewModel: ChannelSearchViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    when (sizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            CompactNavigation(
                modifier = modifier,
                selectedTab = selectedTab,
                onSelectedTabChange = onSelectedTabChange,
                searchViewModel = searchViewModel,
                content = content
            )
        }

        WindowWidthSizeClass.Medium -> {
            MediumNavigation(
                modifier = modifier,
                selectedTab = selectedTab,
                onSelectedTabChange = onSelectedTabChange,
                searchViewModel = searchViewModel,
                content = content
            )
        }

        WindowWidthSizeClass.Expanded -> {
            ExpandedNavigation(
                modifier = modifier,
                selectedTab = selectedTab,
                onSelectedTabChange = onSelectedTabChange,
                searchViewModel = searchViewModel,
                content = content
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactNavigation(
    modifier: Modifier = Modifier,
    selectedTab: Tab,
    onSelectedTabChange: (Tab) -> Unit,
    searchViewModel: ChannelSearchViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MainTopAppBar(
                selectedTab = selectedTab,
                searchViewModel = searchViewModel
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == Tab.Live,
                    label = { Text(stringResource(R.string.live)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null
                        )
                    },
                    onClick = { onSelectedTabChange(Tab.Live) }
                )

                NavigationBarItem(
                    selected = selectedTab == Tab.Followed,
                    label = { Text(stringResource(R.string.channels)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null
                        )
                    },
                    onClick = { onSelectedTabChange(Tab.Followed) }
                )

                NavigationBarItem(
                    selected = selectedTab == Tab.Search,
                    label = { Text(stringResource(R.string.search)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    onClick = { onSelectedTabChange(Tab.Search) }
                )

                NavigationBarItem(
                    selected = selectedTab == Tab.Settings,
                    label = { Text(stringResource(R.string.settings)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    },
                    onClick = { onSelectedTabChange(Tab.Settings) }
                )
            }
        },
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediumNavigation(
    modifier: Modifier = Modifier,
    selectedTab: Tab,
    onSelectedTabChange: (Tab) -> Unit,
    searchViewModel: ChannelSearchViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    Row {
        NavigationRail {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(
                    16.dp,
                    alignment = Alignment.CenterVertically
                ),
            ) {
                NavigationRailItem(
                    selected = selectedTab == Tab.Live,
                    label = {
                        AnimatedVisibility(visible = selectedTab == Tab.Live) {
                            Text(stringResource(R.string.live))
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null
                        )
                    },
                    onClick = { onSelectedTabChange(Tab.Live) }
                )

                NavigationRailItem(
                    selected = selectedTab == Tab.Followed,
                    label = {
                        AnimatedVisibility(visible = selectedTab == Tab.Followed) {
                            Text(stringResource(R.string.channels))
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null
                        )
                    },
                    onClick = { onSelectedTabChange(Tab.Followed) }
                )

                NavigationRailItem(
                    selected = selectedTab == Tab.Search,
                    label = {
                        AnimatedVisibility(visible = selectedTab == Tab.Search) {
                            Text(stringResource(R.string.search))
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    onClick = { onSelectedTabChange(Tab.Search) }
                )

                NavigationRailItem(
                    selected = selectedTab == Tab.Settings,
                    label = {
                        AnimatedVisibility(visible = selectedTab == Tab.Settings) {
                            Text(stringResource(R.string.settings))
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    },
                    onClick = { onSelectedTabChange(Tab.Settings) }
                )
            }
        }

        Scaffold(
            modifier = modifier,
            topBar = {
                MainTopAppBar(
                    selectedTab = selectedTab,
                    searchViewModel = searchViewModel
                )
            },
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedNavigation(
    modifier: Modifier = Modifier,
    selectedTab: Tab,
    onSelectedTabChange: (Tab) -> Unit,
    searchViewModel: ChannelSearchViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    PermanentNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            PermanentDrawerSheet(
                modifier = Modifier
                    .width(240.dp)
                    .padding(vertical = 16.dp)
                    .systemBarsPadding()
            ) {
                NavigationDrawerItem(
                    modifier = Modifier.padding(4.dp),
                    selected = selectedTab == Tab.Live,
                    label = { Text(stringResource(R.string.live)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null
                        )
                    },
                    onClick = { onSelectedTabChange(Tab.Live) }
                )

                NavigationDrawerItem(
                    modifier = Modifier.padding(4.dp),
                    selected = selectedTab == Tab.Followed,
                    label = { Text(stringResource(R.string.channels)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null
                        )
                    },
                    onClick = { onSelectedTabChange(Tab.Followed) }
                )

                NavigationDrawerItem(
                    modifier = Modifier.padding(4.dp),
                    selected = selectedTab == Tab.Search,
                    label = { Text(stringResource(R.string.search)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    onClick = { onSelectedTabChange(Tab.Search) }
                )

                NavigationDrawerItem(
                    modifier = Modifier.padding(4.dp),
                    selected = selectedTab == Tab.Settings,
                    label = { Text(stringResource(R.string.settings)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    },
                    onClick = { onSelectedTabChange(Tab.Settings) }
                )
            }
        },
        content = {
            Scaffold(
                modifier = modifier,
                topBar = {
                    MainTopAppBar(
                        selectedTab = selectedTab,
                        searchViewModel = searchViewModel
                    )
                },
                content = content
            )
        }
    )
}