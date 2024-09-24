package fr.outadoc.justchatting.feature.shared.presentation.mobile

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.onboarding.presentation.mobile.OnboardingScreen
import fr.outadoc.justchatting.feature.shared.presentation.MainRouterViewModel
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.OnLifecycleEvent
import org.koin.compose.koinInject

@Composable
internal fun App(
    onOpenNotificationPreferences: () -> Unit = {},
    onOpenBubblePreferences: () -> Unit = {},
    onOpenAccessibilityPreferences: () -> Unit = {},
    onShareLogs: (Uri) -> Unit = {},
    onShowAuthPage: (Uri) -> Unit = {},
) {
    val viewModel: MainRouterViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    val navController = rememberNavController()

    val onChannelClick = { userId: String ->
        navController.navigate(Screen.Chat(userId).route) {
            launchSingleTop = true
        }
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
            }
        }
    }

    OnLifecycleEvent(
        onStart = { viewModel.onStart() },
    )

    AppTheme {
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
                    )
                }

                is MainRouterViewModel.State.LoggedIn -> {
                    MainRouter(
                        navController = navController,
                        onOpenNotificationPreferences = onOpenNotificationPreferences,
                        onOpenBubblePreferences = onOpenBubblePreferences,
                        onOpenAccessibilityPreferences = onOpenAccessibilityPreferences,
                        onShareLogs = onShareLogs,
                        onChannelClick = onChannelClick,
                    )
                }
            }
        }
    }
}
