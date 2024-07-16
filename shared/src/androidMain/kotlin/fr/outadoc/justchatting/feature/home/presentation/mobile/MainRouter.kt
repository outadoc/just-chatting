package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.DependencyCreditsScreen
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.SettingsContent
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.SettingsSectionAbout
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.SettingsSectionAppearance
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.SettingsSectionNotifications
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.SettingsSectionThirdParties

@Composable
internal fun MainRouter(
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
        startDestination = DefaultScreen,
    ) {
        composable<Screen.Live> {
            LiveChannelsList(
                sizeClass = sizeClass,
                onNavigate = { navController.navigate(it) },
                onItemClick = onChannelClick,
            )
        }

        composable<Screen.Followed> {
            FollowedChannelsList(
                sizeClass = sizeClass,
                onNavigate = { navController.navigate(it) },
                onItemClick = onChannelClick,
            )
        }

        composable<Screen.Settings.Root> {
            SettingsContent(
                sizeClass = sizeClass,
                onNavigate = { navController.navigate(it) },
            )
        }

        composable<Screen.Settings.DependencyCredits> {
            DependencyCreditsScreen(
                onNavigateUp = { navController.popBackStack() },
            )
        }

        composable<Screen.Settings.ThirdParties> {
            SettingsSectionThirdParties(
                onNavigateUp = { navController.popBackStack() },
            )
        }

        composable<Screen.Settings.About> {
            SettingsSectionAbout(
                onNavigateUp = { navController.popBackStack() },
            )
        }

        composable<Screen.Settings.Appearance> {
            SettingsSectionAppearance(
                onNavigateUp = { navController.popBackStack() },
                onOpenAccessibilityPreferences = onOpenAccessibilityPreferences,
            )
        }

        composable<Screen.Settings.Notifications> {
            SettingsSectionNotifications(
                onNavigateUp = { navController.popBackStack() },
                onOpenNotificationPreferences = onOpenNotificationPreferences,
                onOpenBubblePreferences = onOpenBubblePreferences,
            )
        }
    }
}
