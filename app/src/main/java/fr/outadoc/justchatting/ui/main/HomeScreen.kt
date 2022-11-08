package fr.outadoc.justchatting.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.ui.search.channels.ChannelSearchViewModel
import fr.outadoc.justchatting.ui.settings.SettingsContent
import org.koin.androidx.compose.getViewModel

private enum class Tabs {
    Live, Followed, Search, Settings
}

private val DefaultTab = Tabs.Live

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onLogoutClick: () -> Unit,
    onChannelClick: (login: String) -> Unit,
    onOpenNotificationPreferences: () -> Unit,
    onOpenBubblePreferences: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(DefaultTab) }
    val searchViewModel = getViewModel<ChannelSearchViewModel>()
    val searchState by searchViewModel.state.collectAsState()

    BackHandler(
        enabled = selectedTab != DefaultTab,
        onBack = {
            selectedTab = DefaultTab
        }
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            Crossfade(targetState = selectedTab) {
                when (selectedTab) {
                    Tabs.Search -> {
                        SearchTopAppBar(
                            query = searchState.query,
                            onQueryChange = { newQuery ->
                                searchViewModel.onQueryChange(newQuery)
                            }
                        )
                    }

                    else -> {
                        TopAppBar(
                            title = { Text(stringResource(R.string.app_name)) }
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == Tabs.Live,
                    label = { Text(stringResource(R.string.live)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null
                        )
                    },
                    onClick = { selectedTab = Tabs.Live }
                )

                NavigationBarItem(
                    selected = selectedTab == Tabs.Followed,
                    label = { Text(stringResource(R.string.channels)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null
                        )
                    },
                    onClick = { selectedTab = Tabs.Followed }
                )

                NavigationBarItem(
                    selected = selectedTab == Tabs.Search,
                    label = { Text(stringResource(R.string.search)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    onClick = { selectedTab = Tabs.Search }
                )

                NavigationBarItem(
                    selected = selectedTab == Tabs.Settings,
                    label = { Text(stringResource(R.string.settings)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    },
                    onClick = { selectedTab = Tabs.Settings }
                )
            }
        }
    ) { insets ->
        Crossfade(
            modifier = Modifier.padding(insets),
            targetState = selectedTab
        ) {
            when (selectedTab) {
                Tabs.Live -> {
                    LiveChannelsList(
                        onItemClick = { stream ->
                            stream.userLogin?.let { login ->
                                onChannelClick(login)
                            }
                        }
                    )
                }

                Tabs.Followed -> {
                    FollowedChannelsList(
                        onItemClick = { stream ->
                            stream.toLogin?.let { login ->
                                onChannelClick(login)
                            }
                        }
                    )
                }

                Tabs.Search -> {
                    SearchResultsList(
                        onItemClick = { stream ->
                            stream.broadcasterLogin?.let { login ->
                                onChannelClick(login)
                            }
                        },
                        viewModel = searchViewModel
                    )
                }

                Tabs.Settings -> {
                    SettingsContent(
                        onOpenNotificationPreferences = onOpenNotificationPreferences,
                        onOpenBubblePreferences = onOpenBubblePreferences,
                        onLogoutClick = onLogoutClick
                    )
                }
            }
        }
    }
}
