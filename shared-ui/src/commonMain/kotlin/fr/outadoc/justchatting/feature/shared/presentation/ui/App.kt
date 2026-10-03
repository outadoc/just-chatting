package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation3.runtime.rememberNavBackStack
import coil3.SingletonImageLoader
import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.chat.presentation.ui.ImageAnimationPreferences
import fr.outadoc.justchatting.feature.chat.presentation.ui.LocalImageAnimationPreferences
import fr.outadoc.justchatting.feature.onboarding.presentation.ui.ConnectionErrorScreen
import fr.outadoc.justchatting.feature.onboarding.presentation.ui.OnboardingScreen
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.shared.presentation.DefaultScreen
import fr.outadoc.justchatting.feature.shared.presentation.DetailScreen
import fr.outadoc.justchatting.feature.shared.presentation.MainRouterViewModel
import fr.outadoc.justchatting.feature.shared.presentation.ScreenNavBackStackConfig
import fr.outadoc.justchatting.utils.coil.ImageLoaderFactory
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.OnLifecycleEvent
import org.koin.compose.koinInject

@Composable
public fun App(
    onOpenNotificationPreferences: () -> Unit = {},
    onOpenBubblePreferences: () -> Unit = {},
    onShareLogs: (Uri) -> Unit = {},
    onShowAuthPage: (Uri) -> Unit = {},
) {
    val viewModel: MainRouterViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    val preferenceRepository: PreferenceRepository = koinInject()
    val prefs by preferenceRepository.currentPreferences.collectAsState(initial = AppPreferences())
    val imageAnimationPreferences =
        ImageAnimationPreferences(
            animateEmotes = prefs.showAnimatedEmotes,
            animateGifs = prefs.showAnimatedGifs,
        )

    val backStack = rememberNavBackStack(ScreenNavBackStackConfig, DefaultScreen)

    LaunchedEffect(Unit) {
        // Initialize Coil
        SingletonImageLoader.setSafe(ImageLoaderFactory)
    }

    val onChannelClick: (String) -> Unit =
        { userId: String ->
            backStack.navigateToDetail(DetailScreen.Chat(userId))
        }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is MainRouterViewModel.Event.ViewChannel -> {
                    onChannelClick(event.userId)
                }

                is MainRouterViewModel.Event.ShowAuthPage -> {
                    onShowAuthPage(event.uri)
                }

                is MainRouterViewModel.Event.NavigateToTab -> {
                    // Handled by MainRouter
                }
            }
        }
    }

    OnLifecycleEvent(
        onStart = { viewModel.onStart() },
    )

    // Open our own channel links in-app, since not every platform routes them back to us.
    val platformUriHandler = LocalUriHandler.current
    val uriHandler =
        remember(platformUriHandler, viewModel) {
            AppUriHandler(
                platformUriHandler = platformUriHandler,
                onDeeplinkReceived = viewModel::onDeeplinkReceived,
            )
        }

    AppTheme {
        CompositionLocalProvider(
            LocalImageAnimationPreferences provides imageAnimationPreferences,
            LocalUriHandler provides uriHandler,
        ) {
            Crossfade(
                targetState = state,
                label = "Login state animation",
            ) { currentState ->
                when (currentState) {
                    is MainRouterViewModel.State.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is MainRouterViewModel.State.LoggedOut -> {
                        OnboardingScreen(
                            onLoginClick = {
                                viewModel.onLoginClick()
                            },
                            onDemoModeClick = {
                                viewModel.onDemoModeClick()
                            },
                        )
                    }

                    is MainRouterViewModel.State.ConnectionError -> {
                        ConnectionErrorScreen(
                            onRetryClick = {
                                viewModel.onRetryClick()
                            },
                            onLogoutClick = {
                                viewModel.onLogoutClick()
                            },
                        )
                    }

                    is MainRouterViewModel.State.LoggedIn -> {
                        MainRouter(
                            backStack = backStack,
                            onOpenNotificationPreferences = onOpenNotificationPreferences,
                            onOpenBubblePreferences = onOpenBubblePreferences,
                            onShareLogs = onShareLogs,
                            onChannelClick = onChannelClick,
                        )
                    }
                }
            }
        }
    }
}
