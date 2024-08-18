package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.home.domain.model.FullSchedule
import fr.outadoc.justchatting.feature.home.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.feature.home.presentation.EpgViewModel
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.formatTimestamp
import fr.outadoc.justchatting.utils.presentation.plus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EpgScreen(
    modifier: Modifier = Modifier,
    sizeClass: WindowSizeClass,
    onNavigate: (Screen) -> Unit,
    onChannelClick: (userId: String) -> Unit,
) {
    val viewModel: EpgViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    MainNavigation(
        sizeClass = sizeClass,
        selectedScreen = Screen.Epg,
        onSelectedTabChange = onNavigate,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(MR.strings.epg_title)) },
            )
        },
        content = { insets ->
            when (val currentState = state) {
                is EpgViewModel.State.Loading -> {
                    Column(
                        modifier = Modifier
                            .padding(insets)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is EpgViewModel.State.Loaded -> {
                    EpgContent(
                        modifier = modifier,
                        schedule = currentState.schedule,
                        insets = insets,
                        onChannelClick = onChannelClick,
                    )
                }
            }
        },
    )
}

@Composable
private fun EpgContent(
    modifier: Modifier = Modifier,
    schedule: FullSchedule,
    insets: PaddingValues = PaddingValues(),
    onChannelClick: (userId: String) -> Unit,
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = schedule.initialListIndex,
    )

    LaunchedEffect(listState) {
        logDebug<Screen.Epg> { "sharedListState: $listState" }
    }

    EpgVerticalContent(
        modifier = modifier.fillMaxWidth(),
        schedule = schedule,
        listState = listState,
        insets = insets,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EpgVerticalContent(
    modifier: Modifier = Modifier,
    schedule: FullSchedule,
    listState: LazyListState = rememberLazyListState(),
    insets: PaddingValues = PaddingValues(),
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = insets + PaddingValues(
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
                contentType = "date",
            ) {
                DateHeader(date = date)
            }

            items(
                items = schedule.past[date].orEmpty(),
                key = { segment -> segment.id },
                contentType = { "segment" },
            ) { segment ->
                EpgSegment(
                    modifier = Modifier.fillMaxWidth(),
                    segment = segment,
                )
            }
        }

        items(
            schedule.live,
            key = { stream -> stream.id },
            contentType = { "stream" },
        ) { stream ->
            LiveStreamCard(
                modifier = Modifier.fillMaxWidth(),
                title = stream.title,
                userName = stream.userId,
                viewerCount = stream.viewerCount,
                category = stream.category,
                startedAt = stream.startedAt,
                tags = stream.tags,
            )
        }

        schedule.future.keys.forEach { date ->
            stickyHeader(
                key = "future-${date.toEpochDays()}",
                contentType = "date",
            ) {
                DateHeader(date = date)
            }

            items(
                items = schedule.future[date].orEmpty(),
                key = { segment -> segment.id },
                contentType = { "segment" },
            ) { segment ->
                EpgSegment(
                    modifier = Modifier.fillMaxWidth(),
                    segment = segment,
                )
            }
        }
    }
}

@Composable
private fun DateHeader(
    modifier: Modifier = Modifier,
    date: LocalDate,
) {
    Text(
        date.format(),
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier.padding(top = 16.dp),
    )
}

@Composable
internal fun EpgSegment(
    modifier: Modifier = Modifier,
    segment: ChannelScheduleSegment,
    onClick: () -> Unit = {},
) {
    OutlinedCard(
        modifier = modifier,
    ) {
        Column {
            Card(
                onClick = onClick,
            ) {
                EpgSegmentContent(
                    modifier = Modifier.padding(8.dp),
                    title = segment.title,
                    userName = segment.user.displayName,
                    category = segment.category,
                    profileImageUrl = segment.user.profileImageUrl,
                )
            }

            Text(
                modifier = Modifier.padding(8.dp),
                text = buildAnnotatedString {
                    append(segment.startTime.formatTimestamp())
                    append(" - ")
                    append(segment.endTime.formatTimestamp())
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun EpgSegmentContent(
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
                        maxLines = 1,
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
private fun EpgSegmentDetails(
    modifier: Modifier = Modifier,
    segment: ChannelScheduleSegment,
    tz: TimeZone = TimeZone.currentSystemDefault(),
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
                    segment.startTime.toLocalDateTime(tz).date.format()

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
                            append(segment.startTime.formatTimestamp())
                            append(" - ")
                            append(segment.endTime.formatTimestamp())
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

@Composable
private fun LocalDate.format(): String =
    remember(this) {
        toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
    }

@Preview
@Composable
private fun EpgSegmentDetailsPreview(
    @PreviewParameter(LoremIpsum::class) lorem: String,
) {
    AppTheme {
        EpgSegmentDetails(
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
private fun EpgSegmentPreview(
    @PreviewParameter(LoremIpsum::class) lorem: String,
) {
    AppTheme {
        EpgSegment(
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
