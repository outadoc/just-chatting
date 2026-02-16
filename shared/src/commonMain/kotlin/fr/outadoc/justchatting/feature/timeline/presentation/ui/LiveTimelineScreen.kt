package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Today
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.shared.presentation.ui.MainNavigation
import fr.outadoc.justchatting.feature.shared.presentation.ui.Screen
import fr.outadoc.justchatting.feature.timeline.presentation.LiveTimelineViewModel
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.timeline_live
import fr.outadoc.justchatting.shared.timeline_refresh_action_cd
import fr.outadoc.justchatting.shared.timeline_today_action_cd
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import kotlinx.coroutines.launch
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
    val context: PlatformContext = LocalPlatformContext.current

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.syncLiveStreamsPeriodically()
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
                        AccessibleIconButton(
                            onClickLabel = stringResource(Res.string.timeline_today_action_cd),
                            onClick = {
                                coroutineScope.launch {
                                    listState.scrollToItem(index = 0)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null,
                            )
                        }

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
                    },
                )
            }
        },
        content = { insets ->
            LiveTimelineContent(
                modifier = modifier,
                insets = insets,
                live = state.live,
                listState = listState,
                onChannelClick = { user ->
                    onChannelClick(user.id)
                },
                onOpenInBubble = { user ->
                    notifier.notify(
                        context = context,
                        user = user,
                    )
                },
            )
        },
    )
}
