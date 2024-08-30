package fr.outadoc.justchatting.feature.timeline.presentation.mobile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.chat.presentation.mobile.UserInfo
import fr.outadoc.justchatting.feature.chat.presentation.mobile.remoteImageModel
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.mobile.MainNavigation
import fr.outadoc.justchatting.feature.shared.presentation.mobile.Screen
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.FullSchedule
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.timeline.presentation.TimelineViewModel
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.HapticIconButton
import fr.outadoc.justchatting.utils.presentation.formatDate
import fr.outadoc.justchatting.utils.presentation.formatHourMinute
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimelineScreen(
    modifier: Modifier = Modifier,
    sizeClass: WindowSizeClass,
    onNavigate: (Screen) -> Unit,
    onChannelClick: (userId: String) -> Unit,
) {
    val viewModel: TimelineViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    val listState = remember(state.schedule.todayListIndex) {
        LazyListState(
            firstVisibleItemIndex = state.schedule.todayListIndex,
        )
    }

    MainNavigation(
        sizeClass = sizeClass,
        selectedScreen = Screen.Timeline,
        onSelectedTabChange = onNavigate,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            ) {
                TopAppBar(
                    title = { Text(stringResource(MR.strings.timeline_title)) },
                    actions = {
                        val coroutineScope = rememberCoroutineScope()
                        HapticIconButton(
                            onClick = {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(
                                        state.schedule.todayListIndex,
                                    )
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = stringResource(MR.strings.timeline_today_action_cd),
                            )
                        }

                        HapticIconButton(
                            onClick = { viewModel.load() },
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = stringResource(MR.strings.timeline_refresh_action_cd),
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
                listState = listState,
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
    listState: LazyListState,
    onChannelClick: (userId: String) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(insets),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = listState,
    ) {
        schedule.past.keys.forEach { date ->
            stickyHeader(
                key = "past-${date.toEpochDays()}",
                contentType = "header",
            ) {
                SectionHeader(
                    title = { Text(date.formatDate(isFuture = false)) },
                )
            }

            items(
                items = schedule.past[date].orEmpty(),
                key = { segment -> "past-${segment.id}" },
                contentType = { "segment" },
            ) { segment ->
                TimelineSegment(
                    modifier = Modifier.fillMaxWidth(),
                    segment = segment,
                )
            }
        }

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
            key = { userStream -> "live-${userStream.stream.id}" },
            contentType = { "stream" },
        ) { userStream ->
            LiveStreamCard(
                modifier = Modifier.fillMaxWidth(),
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

        schedule.future.keys.forEach { date ->
            stickyHeader(
                key = "future-${date.toEpochDays()}",
                contentType = "header",
            ) {
                SectionHeader(
                    title = { Text(date.formatDate(isFuture = true)) },
                )
            }

            items(
                items = schedule.future[date].orEmpty(),
                key = { segment -> "future-${segment.id}" },
                contentType = { "segment" },
            ) { segment ->
                TimelineSegment(
                    modifier = Modifier.fillMaxWidth(),
                    segment = segment,
                )
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
            LocalTextStyle provides MaterialTheme.typography.headlineSmall,
        ) {
            title()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                )

                Text(
                    buildAnnotatedString {
                        append(segment.startTime.formatHourMinute())
                        append(" - ")
                        append(segment.endTime.formatHourMinute())
                    },
                )
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    if (isExpanded) {
        ModalBottomSheet(
            onDismissRequest = { isExpanded = false },
            sheetState = sheetState,
        ) {
            TimelineSegmentDetails(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                segment = segment,
            )
        }
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        UserInfo(
            user = segment.user,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
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
                            append(" - ")
                            append(segment.endTime.formatHourMinute())
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
    }
}

@Preview
@Composable
private fun TimelineSegmentDetailsPreview(
    @PreviewParameter(LoremIpsum::class) lorem: String,
) {
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
private fun TimelineSegmentPreview(
    @PreviewParameter(LoremIpsum::class) lorem: String,
) {
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
