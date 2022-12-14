package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.feature.home.FollowedChannelsList
import fr.outadoc.justchatting.feature.home.LiveChannelsList
import fr.outadoc.justchatting.feature.home.SearchResultsList
import fr.outadoc.justchatting.feature.home.SearchTopAppBar
import fr.outadoc.justchatting.feature.home.presentation.ChannelSearchViewModel
import fr.outadoc.justchatting.feature.mainnavigation.presentation.mobile.MainNavigation
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.SettingsContent
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    val searchState by searchViewModel.state.collectAsState()

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
        topBar = {
            Crossfade(targetState = selectedTab) { tab ->
                when (tab) {
                    Tab.Search -> {
                        SearchTopAppBar(
                            modifier = modifier,
                            query = searchState.query,
                            onQueryChange = { newQuery ->
                                searchViewModel.onQueryChange(newQuery)
                            }
                        )
                    }

                    else -> {
                        TopAppBar(
                            modifier = modifier,
                            title = { Text(stringResource(R.string.app_name)) }
                        )
                    }
                }
            }
        },
        content = { insets ->
            Crossfade(
                modifier = Modifier.padding(insets),
                targetState = selectedTab
            ) { tab ->
                when (tab) {
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
    )
}
