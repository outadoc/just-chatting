package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.details.presentation.ActionBottomSheet
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.ui.NoContent
import fr.outadoc.justchatting.feature.shared.presentation.ui.SegmentedListDefaults
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.DaySchedule
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.date_today
import fr.outadoc.justchatting.shared.internal.date_today_later
import fr.outadoc.justchatting.shared.internal.date_tomorrow
import fr.outadoc.justchatting.shared.internal.timeline_now
import fr.outadoc.justchatting.utils.presentation.formatShortDay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

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
    selectedDate: LocalDate? = null,
    onSelectedDateChange: (LocalDate) -> Unit = {},
    clock: Clock = Clock.System,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    var showUserDetails: User? by remember { mutableStateOf(null) }

    var now: Instant by remember { mutableStateOf(clock.now()) }

    LaunchedEffect(clock) {
        while (isActive) {
            now = clock.now()
            delay(1.minutes)
        }
    }

    val today: LocalDate = now.toLocalDateTime(timeZone).date

    val selectedDay: DaySchedule? =
        future.firstOrNull { day -> day.date.localDate == selectedDate }
            ?: future.firstOrNull()

    Column(
        modifier =
            modifier
                .padding(top = insets.calculateTopPadding())
                .fillMaxSize(),
    ) {
        if (future.isNotEmpty()) {
            DayChipsRow(
                modifier = Modifier.fillMaxWidth(),
                days = future,
                selectedDate = selectedDay?.date?.localDate,
                today = today,
                onSelectedDateChange = onSelectedDateChange,
            )
        }

        val listInsets = PaddingValues(bottom = insets.calculateBottomPadding())

        if (showRefreshIndicator) {
            val pullToRefreshState = rememberPullToRefreshState()

            PullToRefreshBox(
                modifier = Modifier.fillMaxSize(),
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                },
            ) {
                FutureTimelineList(
                    modifier = Modifier.fillMaxSize(),
                    insets = listInsets,
                    day = selectedDay,
                    isToday = selectedDay?.date?.localDate == today,
                    now = now,
                    listState = listState,
                    onUserClick = { showUserDetails = it },
                )
            }
        } else {
            FutureTimelineList(
                modifier = Modifier.fillMaxSize(),
                insets = listInsets,
                day = selectedDay,
                isToday = selectedDay?.date?.localDate == today,
                now = now,
                listState = listState,
                onUserClick = { showUserDetails = it },
            )
        }
    }

    showUserDetails?.let { user ->
        ActionBottomSheet(
            onDismissRequest = { showUserDetails = null },
            header = {
                ChannelDetailsHeader(user = user)
            },
            content = {
                ChannelDetailsContent(
                    user = user,
                    stream = null,
                )
            },
        )
    }
}

@Composable
private fun DayChipsRow(
    modifier: Modifier = Modifier,
    days: ImmutableList<DaySchedule>,
    selectedDate: LocalDate?,
    today: LocalDate,
    onSelectedDateChange: (LocalDate) -> Unit,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = days,
            key = { day -> day.date.localDate.toEpochDays() },
        ) { day ->
            val date = day.date.localDate
            val isSelected = date == selectedDate

            FilterChip(
                selected = isSelected,
                onClick = { onSelectedDateChange(date) },
                label = {
                    Text(
                        when (date) {
                            today -> stringResource(Res.string.date_today)
                            today + DatePeriod(days = 1) -> stringResource(Res.string.date_tomorrow)
                            else -> date.formatShortDay()
                        },
                    )
                },
                leadingIcon =
                    if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                            )
                        }
                    } else {
                        null
                    },
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
            )
        }
    }
}

@Composable
private fun FutureTimelineList(
    modifier: Modifier = Modifier,
    insets: PaddingValues = PaddingValues(),
    day: DaySchedule?,
    isToday: Boolean,
    now: Instant,
    listState: LazyListState,
    onUserClick: (User) -> Unit,
) {
    val ongoing: List<ChannelScheduleSegment>
    val upcoming: List<ChannelScheduleSegment>

    when {
        day == null -> {
            ongoing = emptyList()
            upcoming = emptyList()
        }

        isToday -> {
            ongoing = day.schedule.filter { segment -> segment.isOngoing(now) }
            upcoming = day.schedule.filter { segment -> segment.startTime > now }
        }

        else -> {
            ongoing = emptyList()
            upcoming = day.schedule
        }
    }

    if (ongoing.isEmpty() && upcoming.isEmpty()) {
        NoContent(
            modifier =
                modifier
                    .padding(insets)
                    .fillMaxSize(),
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            state = listState,
            contentPadding =
                PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp + insets.calculateBottomPadding(),
                ),
            verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.ItemSpacing),
        ) {
            if (ongoing.isNotEmpty()) {
                sectionHeader(
                    key = "header-now",
                    title = { stringResource(Res.string.timeline_now) },
                    isFirst = true,
                )

                segments(
                    segments = ongoing,
                    now = now,
                    onUserClick = onUserClick,
                )
            }

            if (upcoming.isNotEmpty()) {
                if (isToday) {
                    sectionHeader(
                        key = "header-later",
                        title = { stringResource(Res.string.date_today_later) },
                        isFirst = ongoing.isEmpty(),
                    )
                }

                segments(
                    segments = upcoming,
                    now = now,
                    onUserClick = onUserClick,
                )
            }
        }
    }
}

private fun LazyListScope.sectionHeader(
    key: String,
    title: @Composable () -> String,
    isFirst: Boolean,
) {
    item(
        key = key,
        contentType = "header",
    ) {
        Text(
            modifier =
                Modifier
                    .animateItem()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = if (isFirst) 8.dp else 20.dp,
                        bottom = 8.dp,
                    ),
            text = title(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private fun LazyListScope.segments(
    segments: List<ChannelScheduleSegment>,
    now: Instant,
    onUserClick: (User) -> Unit,
) {
    itemsIndexed(
        items = segments,
        key = { _, segment -> segment.id },
        contentType = { _, _ -> "segment" },
    ) { index, segment ->
        FutureTimelineSegment(
            modifier =
                Modifier
                    .animateItem()
                    .fillMaxWidth(),
            segment = segment,
            shape = SegmentedListDefaults.shape(index = index, count = segments.size),
            progress = segment.progressAt(now),
            onUserClick = {
                onUserClick(segment.user)
            },
        )
    }
}

private fun ChannelScheduleSegment.isOngoing(now: Instant): Boolean {
    val endTime = endTime
    return startTime <= now && (endTime == null || now < endTime)
}

private fun ChannelScheduleSegment.progressAt(now: Instant): Float? {
    val endTime = endTime ?: return null
    if (!isOngoing(now)) return null
    val total = endTime - startTime
    val elapsed = now - startTime
    return (elapsed / total).toFloat().coerceIn(0f, 1f)
}
