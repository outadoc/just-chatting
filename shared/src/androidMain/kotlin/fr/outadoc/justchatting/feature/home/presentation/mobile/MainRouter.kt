package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.SettingsContent

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

    Crossfade(
        modifier = modifier,
        targetState = selectedTab,
        label = "Page contents",
    ) { tab ->
        when (tab) {
            Tab.Live -> {
                LiveChannelsList(
                    sizeClass = sizeClass,
                    selectedTab = selectedTab,
                    onSelectedTabChange = { selectedTab = it },
                    onItemClick = onChannelClick,
                )
            }

            Tab.Followed -> {
                FollowedChannelsList(
                    sizeClass = sizeClass,
                    selectedTab = selectedTab,
                    onSelectedTabChange = { selectedTab = it },
                    onItemClick = onChannelClick,
                )
            }

            Tab.Settings -> {
                SettingsContent(
                    sizeClass = sizeClass,
                    selectedTab = selectedTab,
                    onSelectedTabChange = { selectedTab = it },
                    onOpenNotificationPreferences = onOpenNotificationPreferences,
                    onOpenBubblePreferences = onOpenBubblePreferences,
                    onOpenAccessibilityPreferences = onOpenAccessibilityPreferences,
                )
            }
        }
    }
}
