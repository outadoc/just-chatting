package fr.outadoc.justchatting.feature.shared.presentation.mobile

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.chat.presentation.mobile.ChannelChatScreen
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
    onOpenNotificationPreferences: () -> Unit,
    onOpenBubblePreferences: () -> Unit,
    onOpenAccessibilityPreferences: () -> Unit,
    onShareLogs: (Uri) -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = DefaultScreen.route,
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
        composable(
            route = Screen.Followed.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            FollowedChannelsList(
                onNavigate = { navController.navigate(it.route) },
                onItemClick = { userId ->
                    navController.navigate(Screen.Chat(userId).route)
                },
            )
        }

        composable(
            route = Screen.Timeline.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            TimelineScreen(
                onNavigate = { navController.navigate(it.route) },
                onChannelClick = { userId ->
                    navController.navigate(Screen.Chat(userId).route)
                },
            )
        }

        composable(
            route = Screen.Search.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            SearchScreen(
                onNavigate = { navController.navigate(it.route) },
                onChannelClick = { userId ->
                    navController.navigate(Screen.Chat(userId).route)
                },
            )
        }

        composable(
            route = "chat/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                )
            },
        ) { entry ->
            val userId = entry.arguments?.getString("userId")
                ?: error("Missing userId")

            ChannelChatScreen(
                userId = userId,
            )
        }

        composable(
            route = Screen.Settings.Root.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            SettingsContent(
                onNavigate = { navController.navigate(it.route) },
                onShareLogs = onShareLogs,
            )
        }

        composable(
            route = Screen.Settings.About.route,
        ) {
            SettingsSectionAbout(
                onNavigateUp = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.Settings.Appearance.route,
        ) {
            SettingsSectionAppearance(
                onNavigateUp = { navController.popBackStack() },
                onOpenAccessibilityPreferences = onOpenAccessibilityPreferences,
            )
        }

        composable(
            route = Screen.Settings.DependencyCredits.route,
        ) {
            SettingsSectionDependencies(
                onNavigateUp = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.Settings.Notifications.route,
        ) {
            SettingsSectionNotifications(
                onNavigateUp = { navController.popBackStack() },
                onOpenNotificationPreferences = onOpenNotificationPreferences,
                onOpenBubblePreferences = onOpenBubblePreferences,
            )
        }

        composable(
            route = Screen.Settings.ThirdParties.route,
        ) {
            SettingsSectionThirdParties(
                onNavigateUp = { navController.popBackStack() },
            )
        }
    }
}
