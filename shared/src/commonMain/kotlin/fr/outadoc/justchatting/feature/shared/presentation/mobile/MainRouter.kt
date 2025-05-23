package fr.outadoc.justchatting.feature.shared.presentation.mobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)
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
    val scope = rememberCoroutineScope()

    ThreePaneScaffoldPredictiveBackHandler(
        navigator = navigator,
        backBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
    )

    ListDetailPaneScaffold(
        modifier = modifier,
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        paneExpansionDragHandle = { state ->
            DragHandle(state = state)
        },
        listPane = {
            AnimatedPane(
                modifier = Modifier.preferredWidth(500.dp),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = DefaultScreen,
                ) {
                    composable<Screen.Followed> {
                        FollowedChannelsList(
                            onNavigate = { navController.navigate(it) },
                            onItemClick = onChannelClick,
                        )
                    }

                    composable<Screen.Timeline> {
                        TimelineScreen(
                            onNavigate = { navController.navigate(it) },
                            onChannelClick = onChannelClick,
                        )
                    }

                    composable<Screen.Search> {
                        SearchScreen(
                            onNavigate = { navController.navigate(it) },
                            onChannelClick = onChannelClick,
                        )
                    }

                    composable<Screen.Settings> {
                        SettingsContent(
                            onNavigate = { navController.navigate(it) },
                            onNavigateDetails = { screen ->
                                scope.launch {
                                    navigator.navigateTo(
                                        pane = ListDetailPaneScaffoldRole.Detail,
                                        contentKey = screen,
                                    )
                                }
                            },
                            onShareLogs = onShareLogs,
                        )
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane {
                when (val screen = navigator.currentDestination?.contentKey) {
                    is DetailScreen.Chat -> {
                        ChannelChatScreen(
                            userId = screen.id,
                            isStandalone = false,
                            canNavigateUp = navigator.canNavigateBack(),
                            onNavigateUp = {
                                scope.launch {
                                    navigator.navigateBack()
                                }
                            },
                        )
                    }

                    DetailScreen.About -> {
                        SettingsSectionAbout(
                            canNavigateUp = navigator.canNavigateBack(),
                            onNavigateUp = {
                                scope.launch {
                                    navigator.navigateBack()
                                }
                            },
                        )
                    }

                    DetailScreen.Appearance -> {
                        SettingsSectionAppearance(
                            canNavigateUp = navigator.canNavigateBack(),
                            onNavigateUp = {
                                scope.launch {
                                    navigator.navigateBack()
                                }
                            },
                            onOpenAccessibilityPreferences = onOpenAccessibilityPreferences,
                        )
                    }

                    DetailScreen.DependencyCredits -> {
                        SettingsSectionDependencies(
                            canNavigateUp = navigator.canNavigateBack(),
                            onNavigateUp = {
                                scope.launch {
                                    navigator.navigateBack()
                                }
                            },
                        )
                    }

                    DetailScreen.Notifications -> {
                        SettingsSectionNotifications(
                            canNavigateUp = navigator.canNavigateBack(),
                            onNavigateUp = {
                                scope.launch {
                                    navigator.navigateBack()
                                }
                            },
                            onOpenNotificationPreferences = onOpenNotificationPreferences,
                            onOpenBubblePreferences = onOpenBubblePreferences,
                        )
                    }

                    DetailScreen.ThirdParties -> {
                        SettingsSectionThirdParties(
                            canNavigateUp = navigator.canNavigateBack(),
                            onNavigateUp = {
                                scope.launch {
                                    navigator.navigateBack()
                                }
                            },
                        )
                    }

                    null -> {
                        // No detail screen selected
                        NoContent(
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        },
    )
}
