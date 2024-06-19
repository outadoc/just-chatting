package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    val navController = rememberNavController()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = DefaultTab
    ) {
        composable<Tab.Live> {
            LiveChannelsList(
                sizeClass = sizeClass,
                onSelectedTabChange = { navController.navigate(it) },
                onItemClick = onChannelClick,
            )
        }

        composable<Tab.Followed> {
            FollowedChannelsList(
                sizeClass = sizeClass,
                onSelectedTabChange = { navController.navigate(it) },
                onItemClick = onChannelClick,
            )
        }

        composable<Tab.Settings> {
            SettingsContent(
                sizeClass = sizeClass,
                onSelectedTabChange = { navController.navigate(it) },
                onOpenNotificationPreferences = onOpenNotificationPreferences,
                onOpenBubblePreferences = onOpenBubblePreferences,
                onOpenAccessibilityPreferences = onOpenAccessibilityPreferences,
            )
        }
    }
}
