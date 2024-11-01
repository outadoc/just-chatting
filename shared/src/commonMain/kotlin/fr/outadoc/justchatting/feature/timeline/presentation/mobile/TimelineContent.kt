package fr.outadoc.justchatting.feature.timeline.presentation.mobile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import fr.outadoc.justchatting.feature.chat.presentation.mobile.BasicUserInfo
import fr.outadoc.justchatting.feature.chat.presentation.mobile.ExtraUserInfo
import fr.outadoc.justchatting.feature.details.presentation.ActionBottomSheet
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.mobile.NoContent
import fr.outadoc.justchatting.feature.timeline.domain.model.FullSchedule
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.live
import fr.outadoc.justchatting.utils.presentation.formatDate

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun TimelineContent(
    modifier: Modifier = Modifier,
    schedule: FullSchedule,
    insets: PaddingValues = PaddingValues(),
    pagerState: PagerState,
    pastListState: LazyListState,
    liveListState: LazyListState,
    futureListState: LazyListState,
    onChannelClick: (User) -> Unit,
    onOpenInBubble: (User) -> Unit,
) {
    var showUserDetails: User? by remember { mutableStateOf(null) }

    VerticalPager(
        modifier = modifier.padding(insets),
        state = pagerState,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            snapPositionalThreshold = 0.1f,
        ),
    ) { page: Int ->
        when (page) {
            TimelinePages.Past -> {
                if (schedule.past.isEmpty()) {
                    NoContent(
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    state = pastListState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                    ),
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    schedule.past.keys.forEach { date ->
                        stickyHeader(
                            key = "header-${date.toEpochDays()}",
                            contentType = "header",
                        ) {
                            SectionHeader(
                                title = { Text(date.formatDate(isFuture = false)) },
                            )
                        }

                        items(
                            items = schedule.past[date].orEmpty(),
                            key = { segment -> segment.id },
                            contentType = { "segment" },
                        ) { segment ->
                            PastTimelineSegment(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth(),
                                segment = segment,
                                onUserClick = {
                                    showUserDetails = segment.user
                                },
                            )
                        }
                    }
                }
            }

            TimelinePages.Live -> {
                if (schedule.live.isEmpty()) {
                    NoContent(
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        state = liveListState,
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        stickyHeader(
                            key = "live",
                            contentType = "header",
                        ) {
                            if (schedule.live.isNotEmpty()) {
                                SectionHeader(
                                    title = { Text(stringResource(Res.string.live)) },
                                )
                            }
                        }

                        items(
                            schedule.live,
                            key = { userStream -> userStream.stream.id },
                            contentType = { "stream" },
                        ) { userStream ->
                            LiveTimelineSegment(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth(),
                                userStream = userStream,
                                onOpenChat = {
                                    onChannelClick(userStream.user)
                                },
                                onUserClick = {
                                    showUserDetails = userStream.user
                                },
                                onOpenInBubble = {
                                    onOpenInBubble(userStream.user)
                                },
                            )
                        }
                    }
                }
            }

            TimelinePages.Future -> {
                if (schedule.future.isEmpty()) {
                    NoContent(
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        state = futureListState,
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        schedule.future.keys.forEach { date ->
                            stickyHeader(
                                key = "header-${date.toEpochDays()}",
                                contentType = "header",
                            ) {
                                SectionHeader(
                                    title = { Text(date.formatDate(isFuture = true)) },
                                )
                            }

                            items(
                                items = schedule.future[date].orEmpty(),
                                key = { segment -> segment.id },
                                contentType = { "segment" },
                            ) { segment ->
                                FutureTimelineSegment(
                                    modifier = Modifier
                                        .animateItem()
                                        .fillMaxWidth(),
                                    segment = segment,
                                    onUserClick = {
                                        showUserDetails = segment.user
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    showUserDetails?.let { user ->
        ActionBottomSheet(
            onDismissRequest = { showUserDetails = null },
            header = {
                BasicUserInfo(user = user)
            },
            content = {
                ExtraUserInfo(user = user)
            },
        )
    }
}

@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    0f to MaterialTheme.colorScheme.surface,
                    1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                ),
            )
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.titleMedium,
        ) {
            title()
        }
    }
}
