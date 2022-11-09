package fr.outadoc.justchatting.ui.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.ui.search.channels.ChannelSearchViewModel

@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    selectedTab: Tab,
    onSelectedTabChange: (Tab) -> Unit,
    searchViewModel: ChannelSearchViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    CompactNavigation(
        modifier = modifier,
        selectedTab = selectedTab,
        onSelectedTabChange = onSelectedTabChange,
        searchViewModel = searchViewModel,
        content = content
    )
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
                            imageVector = Icons.Default.Star,
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