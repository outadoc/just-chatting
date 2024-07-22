package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.paging.PagingData
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.chat.presentation.mobile.UserInfo
import fr.outadoc.justchatting.feature.chat.presentation.mobile.remoteImageModel
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSchedule
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleForDay
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.home.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.feature.home.presentation.EpgViewModel
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.formatTimestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EpgScreen(
    modifier: Modifier = Modifier,
    sizeClass: WindowSizeClass,
    onNavigate: (Screen) -> Unit,
    onChannelClick: (login: String) -> Unit,
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
                        pagingData = currentState.pagingData,
                        days = currentState.days,
                        contentPadding = insets,
                        onChannelClick = onChannelClick,
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EpgContent(
    modifier: Modifier = Modifier,
    pagingData: Flow<PagingData<ChannelSchedule>>,
    days: List<LocalDate>,
    contentPadding: PaddingValues = PaddingValues(),
    onChannelClick: (login: String) -> Unit,
) {
    val channels: LazyPagingItems<ChannelSchedule> = pagingData.collectAsLazyPagingItems()
    var sharedListState by remember { mutableStateOf(SharedListState()) }

    LaunchedEffect(sharedListState) {
        logDebug<Screen.Epg> { "sharedListState: $sharedListState" }
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
    ) {
        stickyHeader("timeline") {
            Timeline(
                modifier = Modifier
                    .width(48.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                    )
                    .padding(end = 8.dp),
                days = days,
                sharedListState = sharedListState,
                updateSharedListState = { sharedListState = it },
            )

            VerticalDivider()
        }

        items(
            count = channels.itemCount,
            key = { index -> channels[index]?.user?.id ?: index },
            contentType = { "channel" },
        ) { index ->
            val channel: ChannelSchedule? = channels[index]

            if (channel != null) {
                val dayItems = channel.scheduleFlow.collectAsLazyPagingItems()

                EpgChannelEntry(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(ColumnWidth),
                    user = channel.user,
                    days = dayItems,
                    sharedListState = sharedListState,
                    updateSharedListState = { sharedListState = it },
                    onChannelClick = onChannelClick,
                )

                if (index < channels.itemCount - 1) {
                    VerticalDivider()
                }
            } else {
                Spacer(modifier = Modifier.width(ColumnWidth))
            }
        }
    }
}

@Composable
private fun Timeline(
    modifier: Modifier = Modifier,
    days: List<LocalDate>,
    sharedListState: SharedListState = SharedListState(),
    updateSharedListState: (SharedListState) -> Unit = {},
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = sharedListState.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = sharedListState.firstVisibleItemScrollOffset,
    )

    LaunchedEffect(sharedListState) {
        listState.scrollToItem(
            index = sharedListState.firstVisibleItemIndex,
            scrollOffset = sharedListState.firstVisibleItemScrollOffset,
        )
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, scrollOffset) ->
                if (sharedListState.firstVisibleItemIndex != index ||
                    sharedListState.firstVisibleItemScrollOffset != scrollOffset
                ) {
                    updateSharedListState(
                        SharedListState(
                            firstVisibleItemIndex = index,
                            firstVisibleItemScrollOffset = scrollOffset,
                        ),
                    )
                }
            }
    }

    LazyColumn(
        modifier = modifier.fillMaxHeight(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(
            key = "header",
            contentType = "header",
        ) {
            Spacer(modifier = Modifier.height(HeaderHeight))
        }

        items(
            items = days,
            key = { date -> date.toEpochDays() },
            contentType = { "date" },
        ) { date ->
            Column(
                modifier = Modifier
                    .height(heightForDuration(1.days))
                    .fillMaxWidth(),
            ) {
                Text(
                    date.dayOfWeek.getDisplayName(
                        TextStyle.SHORT,
                        Locale.getDefault(),
                    ),
                )

                Text(date.dayOfMonth.toString())
            }

            HorizontalDivider()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EpgChannelEntry(
    modifier: Modifier = Modifier,
    user: User,
    days: LazyPagingItems<ChannelScheduleForDay>,
    sharedListState: SharedListState = SharedListState(),
    updateSharedListState: (SharedListState) -> Unit = {},
    onChannelClick: (login: String) -> Unit,
) {
    var hasSettled by remember { mutableStateOf(false) }

    logDebug<Screen.Epg> { "sharedListState: $sharedListState" }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = sharedListState.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = sharedListState.firstVisibleItemScrollOffset,
    )

    if (days.loadState.isIdle) {
        LaunchedEffect(sharedListState) {
            logDebug<Screen.Epg> { "shared state for ${user.displayName}: $sharedListState" }
            listState.scrollToItem(
                index = sharedListState.firstVisibleItemIndex,
                scrollOffset = sharedListState.firstVisibleItemScrollOffset,
            )
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, scrollOffset) ->
                if (sharedListState.firstVisibleItemIndex == index && sharedListState.firstVisibleItemScrollOffset == scrollOffset) {
                    logDebug<Screen.Epg> { "${user.displayName} has settled ($sharedListState)" }
                    hasSettled = true
                } else if (hasSettled && (index > 0 || scrollOffset > 0)) {
                    logDebug<Screen.Epg> { "updating shared state from ${user.displayName} to ($index, $scrollOffset), was $sharedListState" }
                    updateSharedListState(
                        SharedListState(
                            firstVisibleItemIndex = index,
                            firstVisibleItemScrollOffset = scrollOffset,
                        ),
                    )
                }
            }
    }

    LazyColumn(
        modifier = modifier.fillMaxHeight(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        stickyHeader(
            key = "header",
            contentType = "header",
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            ) {
                Column(
                    modifier = Modifier.height(HeaderHeight),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onChannelClick(user.login) },
                        model = remoteImageModel(user.profileImageUrl),
                        contentDescription = null,
                    )

                    Text(
                        user.displayName,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        items(
            count = days.itemCount,
            key = { index -> days[index]?.date?.toEpochDays() ?: index },
            contentType = { "day" },
        ) { index ->
            val day = days[index]

            if (day == null) {
                Spacer(modifier = Modifier.height(heightForDuration(1.days)))
            } else {
                Column(
                    modifier = Modifier.height(heightForDuration(1.days)),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (segment in day.segments) {
                        EpgSegment(
                            modifier = Modifier.fillMaxWidth(),
                            segment = segment,
                            user = user,
                        )
                    }
                }

                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun EpgSegment(
    modifier: Modifier = Modifier,
    segment: ChannelScheduleSegment,
    user: User,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        onClick = { isExpanded = true },
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
        ) {
            Text(
                buildAnnotatedString {
                    append(segment.startTime.formatTimestamp())
                    append(" - ")
                    append(segment.endTime.formatTimestamp())

                    segment.category?.let { category ->
                        append(" · ")
                        append(category.name)
                    }
                },
                style = MaterialTheme.typography.labelSmall,
            )

            if (segment.title.isNotEmpty()) {
                Text(
                    segment.title,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
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
            EpgSegmentDetails(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                segment = segment,
                user = user,
            )
        }
    }
}

@Composable
private fun EpgSegmentDetails(
    modifier: Modifier = Modifier,
    segment: ChannelScheduleSegment,
    user: User,
    tz: TimeZone = TimeZone.currentSystemDefault(),
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        UserInfo(
            user = user,
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

                val date = remember(segment.startTime) {
                    segment.startTime
                        .toLocalDateTime(tz)
                        .date
                        .toJavaLocalDate()
                        .format(
                            DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL),
                        )
                }

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

@Preview
@Composable
private fun EpgSegmentDetailsPreview(
    @PreviewParameter(LoremIpsum::class) lorem: String,
) {
    AppTheme {
        EpgSegmentDetails(
            segment = ChannelScheduleSegment(
                id = "1",
                title = lorem,
                startTime = Instant.parse("2022-01-01T12:00:00Z"),
                endTime = Instant.parse("2022-01-01T13:00:00Z"),
                category = StreamCategory(
                    id = "1",
                    name = lorem,
                ),
                isRecurring = false,
            ),
            user = User(
                id = "1",
                login = "user",
                displayName = lorem,
            ),
        )
    }
}

@Preview
@Composable
private fun EpgSegmentPreview() {
    AppTheme {
        EpgSegment(
            segment = ChannelScheduleSegment(
                id = "1",
                title = "Title",
                startTime = Instant.parse("2022-01-01T12:00:00Z"),
                endTime = Instant.parse("2022-01-01T13:00:00Z"),
                category = null,
                isRecurring = false,
            ),
            user = User(
                id = "1",
                login = "user",
                displayName = "User",
            ),
        )
    }
}

private fun heightForDuration(duration: Duration): Dp {
    return duration.toDouble(DurationUnit.HOURS) * HourHeight
}

private val HourHeight = 8.dp
private val HeaderHeight = 100.dp
private val ColumnWidth = 200.dp

private data class SharedListState(
    val firstVisibleItemIndex: Int = 0,
    val firstVisibleItemScrollOffset: Int = 0,
)
