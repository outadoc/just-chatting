package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.chat.presentation.ui.ChannelChatScreen
import fr.outadoc.justchatting.feature.followed.presentation.ui.FollowedChannelsList
import fr.outadoc.justchatting.feature.preferences.presentation.ui.SettingsContent
import fr.outadoc.justchatting.feature.preferences.presentation.ui.SettingsSectionAbout
import fr.outadoc.justchatting.feature.preferences.presentation.ui.SettingsSectionAppearance
import fr.outadoc.justchatting.feature.preferences.presentation.ui.SettingsSectionDependencies
import fr.outadoc.justchatting.feature.preferences.presentation.ui.SettingsSectionNotifications
import fr.outadoc.justchatting.feature.preferences.presentation.ui.SettingsSectionThirdParties
import fr.outadoc.justchatting.feature.search.presentation.ui.SearchScreen
import fr.outadoc.justchatting.feature.shared.presentation.DefaultScreen
import fr.outadoc.justchatting.feature.shared.presentation.DetailScreen
import fr.outadoc.justchatting.feature.shared.presentation.MainRouterViewModel
import fr.outadoc.justchatting.feature.shared.presentation.Screen
import fr.outadoc.justchatting.feature.shared.presentation.ScreenNavBackStackConfig
import fr.outadoc.justchatting.feature.timeline.presentation.ui.FutureTimelineScreen
import fr.outadoc.justchatting.feature.timeline.presentation.ui.LiveTimelineScreen
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun MainRouter(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey> = rememberNavBackStack(ScreenNavBackStackConfig, DefaultScreen),
    onOpenNotificationPreferences: () -> Unit = {},
    onOpenBubblePreferences: () -> Unit = {},
    onOpenAccessibilityPreferences: () -> Unit = {},
    onShareLogs: (Uri) -> Unit = {},
    onChannelClick: (String) -> Unit = {},
) {
    val mainRouterViewModel: MainRouterViewModel = koinInject()

    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val directive =
        remember(windowAdaptiveInfo) {
            calculatePaneScaffoldDirective(windowAdaptiveInfo)
                .copy(defaultPanePreferredWidth = 500.dp)
        }
    val isTwoPane = directive.maxHorizontalPartitions > 1
    val listDetailStrategy =
        rememberListDetailSceneStrategy<NavKey>(
            directive = directive,
            paneExpansionDragHandle = { state -> DragHandle(state = state) },
        )

    LaunchedEffect(mainRouterViewModel.events) {
        mainRouterViewModel.events.collect { event ->
            when (event) {
                is MainRouterViewModel.Event.NavigateToTab -> {
                    val openChat = backStack.lastOrNull() as? DetailScreen.Chat
                    backStack.clear()
                    backStack.add(event.screen)
                    openChat?.let(backStack::add)
                }

                else -> {
                    Unit
                }
            }
        }
    }

    val onNavigate: (Screen) -> Unit = { screen ->
        mainRouterViewModel.onTabSelected(screen)
    }

    val detailPlaceholder: @Composable ThreePaneScaffoldScope.() -> Unit = {
        DetailPaneContent(isTwoPane) {
            NoContent(modifier = Modifier.fillMaxSize())
        }
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        sceneStrategies = listOf(listDetailStrategy),
        entryProvider =
            entryProvider {
                entry<Screen.Followed>(
                    metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = detailPlaceholder),
                ) {
                    FollowedChannelsList(
                        onNavigate = onNavigate,
                        onItemClick = onChannelClick,
                    )
                }

                entry<Screen.Live>(
                    metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = detailPlaceholder),
                ) {
                    LiveTimelineScreen(
                        selectedChannelId = (backStack.lastOrNull() as? DetailScreen.Chat)?.id,
                        onNavigate = onNavigate,
                        onChannelClick = onChannelClick,
                    )
                }

                entry<Screen.Future>(
                    metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = detailPlaceholder),
                ) {
                    FutureTimelineScreen(
                        onNavigate = onNavigate,
                    )
                }

                entry<Screen.Search>(
                    metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = detailPlaceholder),
                ) {
                    SearchScreen(
                        onNavigate = onNavigate,
                        onChannelClick = onChannelClick,
                    )
                }

                entry<Screen.Settings>(
                    metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = detailPlaceholder),
                ) {
                    SettingsContent(
                        onNavigate = onNavigate,
                        onNavigateDetails = { screen -> backStack.navigateToDetail(screen) },
                    )
                }

                entry<DetailScreen.Chat>(
                    metadata = ListDetailSceneStrategy.detailPane(),
                ) { screen ->
                    DetailPaneContent(isTwoPane) {
                        ChannelChatScreen(
                            modifier = Modifier.keepScreenOn(),
                            userId = screen.id,
                            isStandalone = false,
                            canNavigateUp = !isTwoPane,
                            onNavigateUp = { backStack.removeLastOrNull() },
                        )
                    }
                }

                entry<DetailScreen.About>(
                    metadata = ListDetailSceneStrategy.detailPane(),
                ) {
                    DetailPaneContent(isTwoPane) {
                        SettingsSectionAbout(
                            canNavigateUp = !isTwoPane,
                            onNavigateUp = { backStack.removeLastOrNull() },
                            onShareLogs = onShareLogs,
                        )
                    }
                }

                entry<DetailScreen.Appearance>(
                    metadata = ListDetailSceneStrategy.detailPane(),
                ) {
                    DetailPaneContent(isTwoPane) {
                        SettingsSectionAppearance(
                            canNavigateUp = !isTwoPane,
                            onNavigateUp = { backStack.removeLastOrNull() },
                            onOpenAccessibilityPreferences = onOpenAccessibilityPreferences,
                        )
                    }
                }

                entry<DetailScreen.DependencyCredits>(
                    metadata = ListDetailSceneStrategy.detailPane(),
                ) {
                    DetailPaneContent(isTwoPane) {
                        SettingsSectionDependencies(
                            canNavigateUp = !isTwoPane,
                            onNavigateUp = { backStack.removeLastOrNull() },
                        )
                    }
                }

                entry<DetailScreen.Notifications>(
                    metadata = ListDetailSceneStrategy.detailPane(),
                ) {
                    DetailPaneContent(isTwoPane) {
                        SettingsSectionNotifications(
                            canNavigateUp = !isTwoPane,
                            onNavigateUp = { backStack.removeLastOrNull() },
                            onOpenNotificationPreferences = onOpenNotificationPreferences,
                            onOpenBubblePreferences = onOpenBubblePreferences,
                        )
                    }
                }

                entry<DetailScreen.ThirdParties>(
                    metadata = ListDetailSceneStrategy.detailPane(),
                ) {
                    DetailPaneContent(isTwoPane) {
                        SettingsSectionThirdParties(
                            canNavigateUp = !isTwoPane,
                            onNavigateUp = { backStack.removeLastOrNull() },
                        )
                    }
                }
            },
    )
}

@Composable
private fun DetailPaneContent(
    isTwoPane: Boolean,
    content: @Composable () -> Unit,
) {
    if (isTwoPane) {
        DetailPaneCard {
            content()
        }
    } else {
        content()
    }
}
