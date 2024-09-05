package fr.outadoc.justchatting.feature.shared.presentation.mobile

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.outadoc.justchatting.feature.followed.presentation.mobile.FollowedChannelsList
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.SettingsContent
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.SettingsSectionAbout
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.SettingsSectionAppearance
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.SettingsSectionDependencies
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.SettingsSectionNotifications
import fr.outadoc.justchatting.feature.preferences.presentation.mobile.SettingsSectionThirdParties
import fr.outadoc.justchatting.feature.search.presentation.mobile.SearchScreen
import fr.outadoc.justchatting.feature.timeline.presentation.mobile.TimelineScreen

@Composable
internal fun MainRouter(
    modifier: Modifier = Modifier,
    sizeClass: WindowSizeClass,
    onChannelClick: (userId: String) -> Unit,
    onOpenNotificationPreferences: () -> Unit,
    onOpenBubblePreferences: () -> Unit,
    onOpenAccessibilityPreferences: () -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = DefaultScreen,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
            )
        },
    ) {
        composable<Screen.Followed>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            FollowedChannelsList(
                onNavigate = { navController.navigate(it) },
                onItemClick = onChannelClick,
            )
        }

        composable<Screen.Timeline>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            TimelineScreen(
                sizeClass = sizeClass,
                onNavigate = { navController.navigate(it) },
                onChannelClick = onChannelClick,
            )
        }

        composable<Screen.Search>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            SearchScreen(
                sizeClass = sizeClass,
                onNavigate = { navController.navigate(it) },
                onChannelClick = onChannelClick,
            )
        }

        composable<Screen.Settings.Root>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            SettingsContent(
                onNavigate = { navController.navigate(it) },
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

        composable<Screen.Settings.DependencyCredits> {
            SettingsSectionDependencies(
                onNavigateUp = { navController.popBackStack() },
            )
        }

        composable<Screen.Settings.Notifications> {
            SettingsSectionNotifications(
                onNavigateUp = { navController.popBackStack() },
                onOpenNotificationPreferences = onOpenNotificationPreferences,
                onOpenBubblePreferences = onOpenBubblePreferences,
            )
        }

        composable<Screen.Settings.ThirdParties> {
            SettingsSectionThirdParties(
                onNavigateUp = { navController.popBackStack() },
            )
        }
    }
}
