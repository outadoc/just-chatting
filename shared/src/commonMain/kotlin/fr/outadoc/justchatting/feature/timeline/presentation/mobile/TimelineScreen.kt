package fr.outadoc.justchatting.feature.timeline.presentation.mobile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.chat.presentation.mobile.UserInfo
import fr.outadoc.justchatting.feature.chat.presentation.mobile.remoteImageModel
import fr.outadoc.justchatting.feature.details.presentation.DetailsDialog
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainNavigation
import fr.outadoc.justchatting.feature.shared.presentation.mobile.NoContent
import fr.outadoc.justchatting.feature.shared.presentation.mobile.Screen
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.FullSchedule
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.timeline.presentation.TimelineViewModel
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.presentation.AccessibleIconButton
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.format
import fr.outadoc.justchatting.utils.presentation.formatDate
import fr.outadoc.justchatting.utils.presentation.formatHourMinute
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.jetbrains.compose.ui.tooling.preview.Preview
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
        initialPage = PAGE_LIVE,
    )

    MainNavigation(
        selectedScreen = Screen.Timeline,
        onSelectedTabChange = onNavigate,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            ) {
                TopAppBar(
                    title = { Text(stringResource(MR.strings.timeline_title)) },
                    actions = {
                        AccessibleIconButton(
                            onClickLabel = stringResource(MR.strings.timeline_today_action_cd),
                            onClick = {
                                coroutineScope.launch {
                                    val currentPage = pagerState.currentPage

                                    liveListState.scrollToItem(index = 0)

                                    if (currentPage != PAGE_LIVE) {
                                        pagerState.animateScrollToPage(page = PAGE_LIVE)
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
                            onClickLabel = stringResource(MR.strings.timeline_refresh_action_cd),
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
                onChannelClick = onChannelClick,
            )
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimelineContent(
    modifier: Modifier = Modifier,
    schedule: FullSchedule,
    insets: PaddingValues = PaddingValues(),
    pagerState: PagerState,
    pastListState: LazyListState,
    liveListState: LazyListState,
    futureListState: LazyListState,
    onChannelClick: (userId: String) -> Unit,
) {
    VerticalPager(
        modifier = modifier.padding(insets),
        state = pagerState,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            snapPositionalThreshold = 0.1f,
        ),
    ) { page: Int ->
        when (page) {
            PAGE_PAST -> {
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
                            TimelineSegment(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth(),
                                segment = segment,
                            )
                        }
                    }
                }
            }

            PAGE_LIVE -> {
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
                                    title = { Text(stringResource(MR.strings.live)) },
                                )
                            }
                        }

                        items(
                            schedule.live,
                            key = { userStream -> userStream.stream.id },
                            contentType = { "stream" },
                        ) { userStream ->
                            LiveStreamCard(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth(),
                                onClick = { onChannelClick(userStream.user.id) },
                                title = userStream.stream.title,
                                userName = userStream.user.displayName,
                                viewerCount = userStream.stream.viewerCount,
                                category = userStream.stream.category,
                                startedAt = userStream.stream.startedAt,
                                tags = userStream.stream.tags.toPersistentSet(),
                                profileImageUrl = userStream.user.profileImageUrl,
                            )
                        }
                    }
                }
            }

            PAGE_FUTURE -> {
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
                                TimelineSegment(
                                    modifier = Modifier
                                        .animateItem()
                                        .fillMaxWidth(),
                                    segment = segment,
                                )
                            }
                        }
                    }
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

@Composable
internal fun TimelineSegment(
    modifier: Modifier = Modifier,
    segment: ChannelScheduleSegment,
) {
    var isExpanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier,
    ) {
        Column {
            Card(
                onClick = { isExpanded = true },
            ) {
                TimelineSegmentContent(
                    modifier = Modifier.padding(8.dp),
                    title = segment.title,
                    userName = segment.user.displayName,
                    category = segment.category,
                    profileImageUrl = segment.user.profileImageUrl,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                )

                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = buildAnnotatedString {
                        append(segment.startTime.formatHourMinute())

                        if (segment.endTime != null) {
                            append(" - ")
                            append(segment.endTime.formatHourMinute())
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(
                    modifier = Modifier.weight(1f, fill = true),
                )

                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Default.Timelapse,
                    contentDescription = null,
                )

                if (segment.endTime != null) {
                    val duration = segment.endTime - segment.startTime
                    Text(
                        modifier = Modifier.alignByBaseline(),
                        text = duration.format(showSeconds = false),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }

    if (isExpanded) {
        DetailsDialog(
            onDismissRequest = { isExpanded = false },
            userDetails = {
                UserInfo(user = segment.user)
            },
            streamDetails = {
                TimelineSegmentDetails(segment = segment)
            },
            actions = {
                ContextualButton(
                    onClick = {},
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null,
                        )
                    },
                    text = {
                        Text(stringResource(MR.strings.watch_live))
                    },
                )

                ContextualButton(
                    onClick = {},
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = null,
                        )
                    },
                    text = {
                        Text("Open chat")
                    },
                )

                ContextualButton(
                    onClick = {},
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = null,
                        )
                    },
                    text = {
                        Text("Open chat")
                    },
                )
            },
        )
    }
}

@Composable
private fun ContextualButton(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            modifier = modifier,
            onClick = onClick,
        ) {
            icon()
        }

        text()
    }
}

@Composable
private fun TimelineSegmentContent(
    modifier: Modifier = Modifier,
    title: String,
    userName: String,
    category: StreamCategory?,
    profileImageUrl: String?,
) {
    Column(
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface),
                model = remoteImageModel(profileImageUrl),
                contentDescription = null,
            )

            Column {
                if (title.isNotEmpty()) {
                    Text(
                        text = title,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                if (userName.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(1f, fill = true)
                                .alignByBaseline(),
                            text = userName,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                category?.let { category ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(1f, fill = true)
                                .alignByBaseline(),
                            text = category.name,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineSegmentDetails(
    modifier: Modifier = Modifier,
    segment: ChannelScheduleSegment,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        if (segment.title.isNotEmpty()) {
            Text(
                segment.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 5,
            )
        }

        val date: String =
            segment.startTime.formatDate()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp),
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
            )

            Text(date)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp),
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
            )

            Text(
                buildAnnotatedString {
                    append(segment.startTime.formatHourMinute())

                    if (segment.endTime != null) {
                        append(" - ")
                        append(segment.endTime.formatHourMinute())
                    }
                },
            )
        }

        segment.category?.let { category ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    imageVector = Icons.Default.Gamepad,
                    contentDescription = null,
                )

                Text(
                    category.name,
                    maxLines = 2,
                )
            }
        }
    }
}

@Preview
@Composable
private fun TimelineSegmentDetailsPreview() {
    val lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
    AppTheme {
        TimelineSegmentDetails(
            segment = ChannelScheduleSegment(
                id = "1",
                user = User(
                    id = "1",
                    login = "user",
                    displayName = lorem,
                    description = "",
                    profileImageUrl = "",
                    createdAt = Instant.DISTANT_PAST,
                    usedAt = Instant.DISTANT_PAST,
                ),
                title = lorem,
                startTime = Instant.parse("2022-01-01T12:00:00Z"),
                endTime = Instant.parse("2022-01-01T13:00:00Z"),
                category = StreamCategory(
                    id = "1",
                    name = lorem,
                ),
            ),
        )
    }
}

@Preview
@Composable
private fun TimelineSegmentPreview() {
    val lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
    AppTheme {
        TimelineSegment(
            segment = ChannelScheduleSegment(
                id = "1",
                user = User(
                    id = "1",
                    login = "user",
                    displayName = lorem,
                    description = "",
                    profileImageUrl = "",
                    createdAt = Instant.DISTANT_PAST,
                    usedAt = Instant.DISTANT_PAST,
                ),
                title = lorem,
                startTime = Instant.parse("2022-01-01T12:00:00Z"),
                endTime = Instant.parse("2022-01-01T13:00:00Z"),
                category = StreamCategory(
                    id = "1",
                    name = lorem,
                ),
            ),
        )
    }
}

private const val PAGE_PAST = 0
private const val PAGE_LIVE = 1
private const val PAGE_FUTURE = 2
