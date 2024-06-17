package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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

    Crossfade(
        targetState = selectedTab,
        label = "Page contents",
    ) { tab ->
        when (tab) {
            Tab.Live -> {
                LiveChannelsList(
                    sizeClass = sizeClass,
                    selectedTab = selectedTab,
                    onSelectedTabChange = { selectedTab = it },
                    onItemClick = { stream ->
                        onChannelClick(stream.userLogin)
                    },
                )
            }

            Tab.Followed -> {
                FollowedChannelsList(
                    selectedTab = selectedTab,
                    onSelectedTabChange = { selectedTab = it },
                    onItemClick = { stream ->
                        onChannelClick(stream.userLogin)
                    },
                )
            }

            Tab.Settings -> {
                SettingsContent(
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
