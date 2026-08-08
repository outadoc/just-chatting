package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.shared.presentation.Screen
import fr.outadoc.justchatting.feature.shared.presentation.ui.MainNavigation
import fr.outadoc.justchatting.feature.timeline.presentation.LiveTimelineViewModel
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.timeline_live
import fr.outadoc.justchatting.shared.internal.timeline_refresh_action_cd
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import fr.outadoc.justchatting.utils.presentation.rememberHasPointingDevice
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LiveTimelineScreen(
    modifier: Modifier = Modifier,
    onNavigate: (Screen) -> Unit,
    onChannelClick: (userId: String) -> Unit,
) {
    val viewModel: LiveTimelineViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    val notifier: ChatNotifier = koinInject()

    val hasMouse = rememberHasPointingDevice()

    LaunchedEffect(Unit) {
        viewModel.syncLiveStreamsPeriodically()
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is LiveTimelineViewModel.Event.NavigateToChannel -> {
                    onChannelClick(event.userId)
                }
            }
        }
    }

    val listState = rememberLazyListState()

    MainNavigation(
        selectedScreen = Screen.Live,
        onSelectedTabChange = onNavigate,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            ) {
                TopAppBar(
                    title = { Text(stringResource(Res.string.timeline_live)) },
                    actions = {
                        if (hasMouse) {
                            AccessibleIconButton(
                                onClickLabel = stringResource(Res.string.timeline_refresh_action_cd),
                                onClick = { viewModel.syncLiveStreamsNow() },
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                    )
                                }
                            }
                        }
                    },
                )
            }
        },
        content = { insets ->
            LiveTimelineContent(
                modifier = modifier,
                insets = insets,
                live = state.live,
                isRefreshing = state.isLoading,
                onRefresh = { viewModel.syncLiveStreamsNow() },
                showRefreshIndicator = !hasMouse,
                listState = listState,
                onChannelClick = { user ->
                    viewModel.onChannelClick(user.id)
                },
                onOpenInBubble = { user ->
                    notifier.notify(user = user)
                },
            )
        },
    )
}
