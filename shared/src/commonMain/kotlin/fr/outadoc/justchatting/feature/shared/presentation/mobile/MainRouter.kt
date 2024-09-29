package fr.outadoc.justchatting.feature.shared.presentation.mobile

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
    chatNavigator: ThreePaneScaffoldNavigator<ChatScreen> = rememberListDetailPaneScaffoldNavigator<ChatScreen>(),
    settingsNavigator: ThreePaneScaffoldNavigator<SettingsSubScreen> = rememberListDetailPaneScaffoldNavigator<SettingsSubScreen>(),
    onOpenNotificationPreferences: () -> Unit = {},
    onOpenBubblePreferences: () -> Unit = {},
    onOpenAccessibilityPreferences: () -> Unit = {},
    onShareLogs: (Uri) -> Unit = {},
    onChannelClick: (String) -> Unit = {},
    preferredListWidth: Dp = 500.dp,
) {
    NavHost(
        navController = navController,
        startDestination = DefaultScreen.route,
    ) {
        composable(
            route = Screen.Timeline.route,
        ) {
            ListDetailPaneScaffold(
                modifier = modifier,
                directive = chatNavigator.scaffoldDirective,
                value = chatNavigator.scaffoldValue,
                listPane = {
                    AnimatedPane(
                        modifier = Modifier.preferredWidth(preferredListWidth),
                    ) {
                        TimelineScreen(
                            onNavigate = { navController.navigate(it.route) },
                            onChannelClick = onChannelClick,
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        chatNavigator.currentDestination?.content?.let { screen ->
                            BackHandler(chatNavigator.canNavigateBack()) {
                                chatNavigator.navigateBack()
                            }

                            ChannelChatScreen(
                                modifier = modifier,
                                userId = screen.id,
                                isStandalone = false,
                                canNavigateUp = chatNavigator.canNavigateBack(),
                                onNavigateUp = { chatNavigator.navigateBack() },
                            )
                        }
                    }
                },
            )
        }

        composable(
            route = Screen.Followed.route,
        ) {
            ListDetailPaneScaffold(
                modifier = modifier,
                directive = chatNavigator.scaffoldDirective,
                value = chatNavigator.scaffoldValue,
                listPane = {
                    AnimatedPane(
                        modifier = Modifier.preferredWidth(preferredListWidth),
                    ) {
                        FollowedChannelsList(
                            onNavigate = { navController.navigate(it.route) },
                            onItemClick = onChannelClick,
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        chatNavigator.currentDestination?.content?.let { screen ->
                            BackHandler(chatNavigator.canNavigateBack()) {
                                chatNavigator.navigateBack()
                            }

                            ChannelChatScreen(
                                modifier = modifier,
                                userId = screen.id,
                                isStandalone = false,
                                canNavigateUp = chatNavigator.canNavigateBack(),
                                onNavigateUp = { chatNavigator.navigateBack() },
                            )
                        }
                    }
                },
            )
        }

        composable(
            route = Screen.Search.route,
        ) {
            ListDetailPaneScaffold(
                modifier = modifier,
                directive = chatNavigator.scaffoldDirective,
                value = chatNavigator.scaffoldValue,
                listPane = {
                    AnimatedPane(
                        modifier = Modifier.preferredWidth(preferredListWidth),
                    ) {
                        SearchScreen(
                            onNavigate = { navController.navigate(it.route) },
                            onChannelClick = onChannelClick,
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        chatNavigator.currentDestination?.content?.let { screen ->
                            BackHandler(chatNavigator.canNavigateBack()) {
                                chatNavigator.navigateBack()
                            }

                            ChannelChatScreen(
                                modifier = modifier,
                                userId = screen.id,
                                isStandalone = false,
                                canNavigateUp = chatNavigator.canNavigateBack(),
                                onNavigateUp = { chatNavigator.navigateBack() },
                            )
                        }
                    }
                },
            )
        }

        composable(
            route = Screen.Settings.Root.route,
        ) {
            ListDetailPaneScaffold(
                modifier = modifier,
                directive = chatNavigator.scaffoldDirective,
                value = chatNavigator.scaffoldValue,
                listPane = {
                    AnimatedPane(
                        modifier = Modifier.preferredWidth(preferredListWidth),
                    ) {
                        SettingsContent(
                            onNavigate = { navController.navigate(it.route) },
                            onNavigateDetails = { screen ->
                                settingsNavigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    content = screen,
                                )
                            },
                            onShareLogs = onShareLogs,
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        settingsNavigator.currentDestination?.content?.let { screen ->
                            BackHandler(settingsNavigator.canNavigateBack()) {
                                settingsNavigator.navigateBack()
                            }

                            when (screen) {
                                SettingsSubScreen.About -> {
                                    SettingsSectionAbout(
                                        modifier = modifier,
                                        canNavigateUp = settingsNavigator.canNavigateBack(),
                                        onNavigateUp = { settingsNavigator.navigateBack() },
                                    )
                                }

                                SettingsSubScreen.Appearance -> {
                                    SettingsSectionAppearance(
                                        modifier = modifier,
                                        canNavigateUp = settingsNavigator.canNavigateBack(),
                                        onNavigateUp = { settingsNavigator.navigateBack() },
                                        onOpenAccessibilityPreferences = onOpenAccessibilityPreferences,
                                    )
                                }

                                SettingsSubScreen.DependencyCredits -> {
                                    SettingsSectionDependencies(
                                        modifier = modifier,
                                        canNavigateUp = settingsNavigator.canNavigateBack(),
                                        onNavigateUp = { settingsNavigator.navigateBack() },
                                    )
                                }

                                SettingsSubScreen.Notifications -> {
                                    SettingsSectionNotifications(
                                        modifier = modifier,
                                        canNavigateUp = settingsNavigator.canNavigateBack(),
                                        onNavigateUp = { settingsNavigator.navigateBack() },
                                        onOpenNotificationPreferences = onOpenNotificationPreferences,
                                        onOpenBubblePreferences = onOpenBubblePreferences,
                                    )
                                }

                                SettingsSubScreen.ThirdParties -> {
                                    SettingsSectionThirdParties(
                                        modifier = modifier,
                                        canNavigateUp = settingsNavigator.canNavigateBack(),
                                        onNavigateUp = { settingsNavigator.navigateBack() },
                                    )
                                }
                            }
                        }
                    }
                },
            )
        }
    }
}
