package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.SettingsContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRouter(
    modifier: Modifier = Modifier,
    sizeClass: WindowSizeClass,
    onChannelClick: (login: String) -> Unit,
    onOpenNotificationPreferences: () -> Unit,
    onOpenBubblePreferences: () -> Unit,
    onOpenAccessibilityPreferences: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(DefaultTab) }

    BackHandler(
        enabled = selectedTab != DefaultTab,
        onBack = {
            selectedTab = DefaultTab
        },
    )

    MainNavigation(
        modifier = modifier,
        sizeClass = sizeClass,
        selectedTab = selectedTab,
        onSelectedTabChange = { selectedTab = it },
        topBar = {
            Crossfade(
                targetState = selectedTab,
                label = "Top app bar cross fade",
            ) { tab ->
                when (tab) {
                    Tab.Live,
                    Tab.Followed,
                    -> {
                        SearchBar(
                            onChannelClick = onChannelClick,
                            sizeClass = sizeClass,
                        )
                    }

                    Tab.Settings -> {
                        TopAppBar(
                            title = { Text(stringResource(fr.outadoc.justchatting.shared.MR.strings.settings)) },
                        )
                    }
                }
            }
        },
        content = { insets ->
            Crossfade(
                modifier = Modifier.padding(insets),
                targetState = selectedTab,
                label = "Page contents",
            ) { tab ->
                when (tab) {
                    Tab.Live -> {
                        LiveChannelsList(
                            onItemClick = { stream ->
                                onChannelClick(stream.userLogin)
                            },
                        )
                    }

                    Tab.Followed -> {
                        FollowedChannelsList(
                            onItemClick = { stream ->
                                stream.userLogin?.let { login ->
                                    onChannelClick(login)
                                }
                            },
                        )
                    }

                    Tab.Settings -> {
                        SettingsContent(
                            onOpenNotificationPreferences = onOpenNotificationPreferences,
                            onOpenBubblePreferences = onOpenBubblePreferences,
                            onOpenAccessibilityPreferences = onOpenAccessibilityPreferences,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    onChannelClick: (String) -> Unit,
    sizeClass: WindowSizeClass,
) {
    // Talkback focus order sorts based on x and y position before considering z-index. The
    // extra Box with semantics and fillMaxWidth is a workaround to get the search bar to focus
    // before the content.
    Box(
        modifier
            .semantics { isTraversalGroup = true }
            .zIndex(1f)
            .fillMaxWidth(),
    ) {
        SearchScreenBar(
            modifier = Modifier
                .fillMaxWidth(),
            onChannelClick = onChannelClick,
            sizeClass = sizeClass,
        )
    }
}
