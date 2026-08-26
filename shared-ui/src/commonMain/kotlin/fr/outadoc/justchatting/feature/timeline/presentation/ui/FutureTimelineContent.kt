package fr.outadoc.justchatting.feature.timeline.presentation.ui

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.presentation.ui.BasicUserInfo
import fr.outadoc.justchatting.feature.chat.presentation.ui.ExtraUserInfo
import fr.outadoc.justchatting.feature.details.presentation.ActionBottomSheet
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.ui.NoContent
import fr.outadoc.justchatting.feature.timeline.domain.model.DaySchedule
import fr.outadoc.justchatting.utils.presentation.formatDate
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun FutureTimelineContent(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    future: ImmutableList<DaySchedule>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    showRefreshIndicator: Boolean,
    listState: LazyListState,
) {
    var showUserDetails: User? by remember { mutableStateOf(null) }

    if (showRefreshIndicator) {
        val pullToRefreshState = rememberPullToRefreshState()

        PullToRefreshBox(
            modifier = modifier.fillMaxSize(),
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = insets.calculateTopPadding()),
                )
            },
        ) {
            FutureTimelineList(
                modifier = Modifier.fillMaxSize(),
                insets = insets,
                future = future,
                listState = listState,
                onUserClick = { showUserDetails = it },
            )
        }
    } else {
        FutureTimelineList(
            modifier = modifier.fillMaxSize(),
            insets = insets,
            future = future,
            listState = listState,
            onUserClick = { showUserDetails = it },
        )
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
private fun FutureTimelineList(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    future: ImmutableList<DaySchedule>,
    listState: LazyListState,
    onUserClick: (User) -> Unit,
) {
    if (future.isEmpty()) {
        NoContent(
            modifier =
                modifier
                    .padding(insets)
                    .fillMaxSize(),
        )
    } else {
        LazyColumn(
            modifier =
                modifier
                    .padding(insets)
                    .fillMaxWidth(),
            state = listState,
            contentPadding =
                PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            future.forEach { daySchedule ->
                stickyHeader(
                    key = "header-${daySchedule.date.localDate.toEpochDays()}",
                    contentType = "header",
                ) {
                    SectionHeader(
                        title = { Text(daySchedule.date.localDate.formatDate(isFuture = true)) },
                    )
                }

                items(
                    items = daySchedule.schedule,
                    key = { segment -> segment.id },
                    contentType = { "segment" },
                ) { segment ->
                    FutureTimelineSegment(
                        modifier =
                            Modifier
                                .animateItem()
                                .fillMaxWidth(),
                        segment = segment,
                        onUserClick = {
                            onUserClick(segment.user)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .background(
                    Brush.verticalGradient(
                        0f to MaterialTheme.colorScheme.surface,
                        1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    ),
                ).padding(vertical = 8.dp)
                .fillMaxWidth(),
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.titleMedium,
        ) {
            title()
        }
    }
}
