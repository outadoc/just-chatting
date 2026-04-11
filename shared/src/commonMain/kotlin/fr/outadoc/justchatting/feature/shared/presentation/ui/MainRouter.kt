package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
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
import fr.outadoc.justchatting.feature.shared.presentation.MainRouterViewModel
import fr.outadoc.justchatting.feature.timeline.presentation.ui.FutureTimelineScreen
import fr.outadoc.justchatting.feature.timeline.presentation.ui.LiveTimelineScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun MainRouter(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey> = rememberNavBackStack(ScreenNavBackStackConfig, DefaultScreen),
    navigator: ThreePaneScaffoldNavigator<DetailScreen> = rememberListDetailPaneScaffoldNavigator<DetailScreen>(),
    onOpenNotificationPreferences: () -> Unit = {},
    onOpenBubblePreferences: () -> Unit = {},
    onOpenAccessibilityPreferences: () -> Unit = {},
    onShareLogs: (Uri) -> Unit = {},
    onChannelClick: (String) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val mainRouterViewModel: MainRouterViewModel = koinInject()

    ThreePaneScaffoldPredictiveBackHandler(
        navigator = navigator,
        backBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
    )

    LaunchedEffect(mainRouterViewModel.events) {
        mainRouterViewModel.events.collect { event ->
            when (event) {
                is MainRouterViewModel.Event.NavigateToTab -> {
                    backStack.clear()
                    backStack.add(event.screen)
                }
                else -> Unit
            }
        }
    }

    val onNavigate: (Screen) -> Unit = { screen ->
        mainRouterViewModel.onTabSelected(screen)
    }

    val saveableDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
    val entries = rememberDecoratedNavEntries<NavKey>(
        backStack = backStack,
        entryDecorators = listOf(saveableDecorator),
        entryProvider = entryProvider {
            entry<Screen.Followed> {
                FollowedChannelsList(
                    onNavigate = onNavigate,
                    onItemClick = onChannelClick,
                )
            }

            entry<Screen.Live> {
                LiveTimelineScreen(
                    onNavigate = onNavigate,
                    onChannelClick = onChannelClick,
                )
            }

            entry<Screen.Future> {
                FutureTimelineScreen(
                    onNavigate = onNavigate,
                )
            }

            entry<Screen.Search> {
                SearchScreen(
                    onNavigate = onNavigate,
                    onChannelClick = onChannelClick,
                )
            }

            entry<Screen.Settings> {
                SettingsContent(
                    onNavigate = onNavigate,
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
        },
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
                NavDisplay(
                    entries = entries,
                    onBack = { backStack.removeLastOrNull() },
                )
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
