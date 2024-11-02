package fr.outadoc.justchatting.feature.timeline.presentation.mobile

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
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
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainNavigation
import fr.outadoc.justchatting.feature.shared.presentation.mobile.Screen
import fr.outadoc.justchatting.feature.timeline.presentation.TimelineViewModel
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.timeline_refresh_action_cd
import fr.outadoc.justchatting.shared.timeline_title
import fr.outadoc.justchatting.shared.timeline_today_action_cd
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
internal fun TimelineScreen(
    modifier: Modifier = Modifier,
    onNavigate: (Screen) -> Unit,
    onChannelClick: (userId: String) -> Unit,
) {
    val viewModel: TimelineViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    val notifier: ChatNotifier = koinInject()
    val context: PlatformContext = LocalPlatformContext.current

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.syncEverythingNow()
        viewModel.syncLiveStreamsPeriodically()
    }

    val pastListState = rememberLazyListState()
    val liveListState = rememberLazyListState()
    val futureListState = rememberLazyListState()

    val pagerState = rememberPagerState(
        pageCount = { 3 },
        initialPage = TimelinePages.Live,
    )

    MainNavigation(
        selectedScreen = Screen.Timeline,
        onSelectedTabChange = onNavigate,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            ) {
                TopAppBar(
                    title = { Text(stringResource(Res.string.timeline_title)) },
                    actions = {
                        AccessibleIconButton(
                            onClickLabel = stringResource(Res.string.timeline_today_action_cd),
                            onClick = {
                                coroutineScope.launch {
                                    val currentPage = pagerState.currentPage

                                    liveListState.scrollToItem(index = 0)

                                    if (currentPage != TimelinePages.Live) {
                                        pagerState.animateScrollToPage(
                                            page = TimelinePages.Live,
                                        )
                                    }

                                    launch {
                                        pastListState.scrollToItem(index = 0)
                                        futureListState.scrollToItem(index = 0)
                                    }
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
                            onClick = { viewModel.syncEverythingNow() },
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
            TimelineContent(
                modifier = modifier,
                schedule = state.schedule,
                insets = insets,
                pastListState = pastListState,
                liveListState = liveListState,
                futureListState = futureListState,
                pagerState = pagerState,
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
