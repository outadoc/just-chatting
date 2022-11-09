package fr.outadoc.justchatting.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.ui.search.channels.ChannelSearchViewModel
import fr.outadoc.justchatting.ui.settings.SettingsContent
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    sizeClass: WindowSizeClass,
    onChannelClick: (login: String) -> Unit,
    onOpenNotificationPreferences: () -> Unit,
    onOpenBubblePreferences: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(DefaultTab) }
    val searchViewModel = getViewModel<ChannelSearchViewModel>()

    BackHandler(
        enabled = selectedTab != DefaultTab,
        onBack = {
            selectedTab = DefaultTab
        }
    )

    MainNavigation(
        modifier = modifier,
        sizeClass = sizeClass,
        selectedTab = selectedTab,
        onSelectedTabChange = { selectedTab = it },
        searchViewModel = searchViewModel
    ) { insets ->
        Crossfade(
            modifier = Modifier.padding(insets),
            targetState = selectedTab
        ) {
            when (selectedTab) {
                Tab.Live -> {
                    LiveChannelsList(
                        onItemClick = { stream ->
                            stream.userLogin?.let { login ->
                                onChannelClick(login)
                            }
                        }
                    )
                }

                Tab.Followed -> {
                    FollowedChannelsList(
                        onItemClick = { stream ->
                            stream.toLogin?.let { login ->
                                onChannelClick(login)
                            }
                        }
                    )
                }

                Tab.Search -> {
                    SearchResultsList(
                        onItemClick = { stream ->
                            stream.broadcasterLogin?.let { login ->
                                onChannelClick(login)
                            }
                        },
                        viewModel = searchViewModel
                    )
                }

                Tab.Settings -> {
                    SettingsContent(
                        onOpenNotificationPreferences = onOpenNotificationPreferences,
                        onOpenBubblePreferences = onOpenBubblePreferences
                    )
                }
            }
        }
    }
}
