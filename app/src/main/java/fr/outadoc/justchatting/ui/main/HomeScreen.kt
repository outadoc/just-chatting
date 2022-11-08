package fr.outadoc.justchatting.ui.main

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.model.helix.follows.Follow
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.ui.settings.SettingsContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onLogoutClick: () -> Unit,
    onStreamClick: (Stream) -> Unit,
    onFollowClick: (Follow) -> Unit,
    onOpenNotificationPreferences: () -> Unit,
    onOpenBubblePreferences: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    Scaffold(
        modifier = modifier,
        topBar = {
            HomeTopAppBar(
                onLogoutClick = onLogoutClick
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    label = { Text(stringResource(R.string.live)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null
                        )
                    },
                    onClick = { selectedTabIndex = 0 }
                )

                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    label = { Text(stringResource(R.string.channels)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null
                        )
                    },
                    onClick = { selectedTabIndex = 1 }
                )

                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    label = { Text(stringResource(R.string.search)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    onClick = { selectedTabIndex = 2 }
                )

                NavigationBarItem(
                    selected = selectedTabIndex == 3,
                    label = { Text(stringResource(R.string.settings)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    },
                    onClick = { selectedTabIndex = 3 }
                )
            }
        }
    ) { insets ->
        Crossfade(
            modifier = Modifier.padding(insets),
            targetState = selectedTabIndex
        ) {
            when (selectedTabIndex) {
                0 -> LiveChannelsList(
                    onItemClick = onStreamClick
                )

                1 -> FollowedChannelsList(
                    onItemClick = onFollowClick
                )

                2 -> {}

                3 -> SettingsContent(
                    onOpenNotificationPreferences = onOpenNotificationPreferences,
                    onOpenBubblePreferences = onOpenBubblePreferences
                )

                else -> error("Unsupported tab $selectedTabIndex")
            }
        }
    }
}
