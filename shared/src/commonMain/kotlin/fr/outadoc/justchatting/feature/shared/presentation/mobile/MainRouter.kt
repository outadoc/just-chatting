package fr.outadoc.justchatting.feature.shared.presentation.mobile

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import fr.outadoc.justchatting.utils.presentation.BackHandler

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun MainRouter(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    navigator: ThreePaneScaffoldNavigator<DetailScreen> = rememberListDetailPaneScaffoldNavigator<DetailScreen>(),
    onOpenNotificationPreferences: () -> Unit = {},
    onOpenBubblePreferences: () -> Unit = {},
    onOpenAccessibilityPreferences: () -> Unit = {},
    onShareLogs: (Uri) -> Unit = {},
    onChannelClick: (String) -> Unit = {},
) {
    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
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
                        enterTransition = { fadeIn() },
                        exitTransition = { fadeOut() },
                    ) {
                        FollowedChannelsList(
                            onNavigate = { navController.navigate(it.route) },
                            onItemClick = onChannelClick,
                        )
                    }

                    composable(
                        route = Screen.Timeline.route,
                        enterTransition = { fadeIn() },
                        exitTransition = { fadeOut() },
                    ) {
                        TimelineScreen(
                            onNavigate = { navController.navigate(it.route) },
                            onChannelClick = onChannelClick,
                        )
                    }

                    composable(
                        route = Screen.Search.route,
                        enterTransition = { fadeIn() },
                        exitTransition = { fadeOut() },
                    ) {
                        SearchScreen(
                            onNavigate = { navController.navigate(it.route) },
                            onChannelClick = onChannelClick,
                        )
                    }

                    composable(
                        route = Screen.Settings.Root.route,
                        enterTransition = { fadeIn() },
                        exitTransition = { fadeOut() },
                    ) {
                        SettingsContent(
                            onNavigate = { navController.navigate(it.route) },
                            onNavigateDetails = { screen ->
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    content = screen
                                )
                            },
                            onShareLogs = onShareLogs,
                        )
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane {
                navigator.currentDestination?.content?.let { screen ->
                    when (screen) {
                        is DetailScreen.Chat -> {
                            ChannelChatScreen(
                                userId = screen.id,
                                isStandalone = false,
                                onNavigateUp = { navigator.navigateBack() },
                            )
                        }

                        DetailScreen.About -> {
                            SettingsSectionAbout(
                                onNavigateUp = { navigator.navigateBack() },
                            )
                        }

                        DetailScreen.Appearance -> {
                            SettingsSectionAppearance(
                                onNavigateUp = { navigator.navigateBack() },
                                onOpenAccessibilityPreferences = onOpenAccessibilityPreferences,
                            )
                        }

                        DetailScreen.DependencyCredits -> {
                            SettingsSectionDependencies(
                                onNavigateUp = { navigator.navigateBack() },
                            )
                        }

                        DetailScreen.Notifications -> {
                            SettingsSectionNotifications(
                                onNavigateUp = { navigator.navigateBack() },
                                onOpenNotificationPreferences = onOpenNotificationPreferences,
                                onOpenBubblePreferences = onOpenBubblePreferences,
                            )
                        }

                        DetailScreen.ThirdParties -> {
                            SettingsSectionThirdParties(
                                onNavigateUp = { navigator.navigateBack() },
                            )
                        }
                    }
                }
            }
        },
    )
}
