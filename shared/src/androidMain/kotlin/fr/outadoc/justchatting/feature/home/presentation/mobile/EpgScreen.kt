package fr.outadoc.justchatting.feature.home.presentation.mobile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.paging.PagingData
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.chat.presentation.mobile.remoteImageModel
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSchedule
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.feature.home.presentation.EpgViewModel
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
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
                        contentPadding = insets,
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
    contentPadding: PaddingValues = PaddingValues(),
) {
    val items = pagingData.collectAsLazyPagingItems()
    var scrollDelta by remember { mutableFloatStateOf(0f) }

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
                scrollDelta = scrollDelta,
                onScrollDeltaChange = { delta -> scrollDelta = delta },
            )

            VerticalDivider()
        }

        items(
            count = items.itemCount,
            key = { index -> items[index]?.user?.id ?: index },
            contentType = { "channel" },
        ) { index ->
            val item: ChannelSchedule? = items[index]

            if (item != null) {
                val segments = item.segments.collectAsLazyPagingItems()

                EpgChannelEntry(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(ColumnWidth),
                    user = item.user,
                    segments = segments,
                    scrollDelta = scrollDelta,
                    onScrollDeltaChange = { delta -> scrollDelta = delta },
                )

                if (index < items.itemCount - 1) {
                    VerticalDivider()
                }
            }
        }
    }
}

@Composable
private fun Timeline(
    modifier: Modifier = Modifier,
    currentTime: Instant = Clock.System.now(),
    tz: TimeZone = TimeZone.currentSystemDefault(),
    scrollDelta: Float = 0f,
    onScrollDeltaChange: (Float) -> Unit = {},
) {
    val today = currentTime.toLocalDateTime(tz).date
    val nextMonth = buildList {
        for (i in 0..30) {
            add(today.plus(i, DateTimeUnit.DAY))
        }
    }

    val scrollState = rememberLazyListState()

    LaunchedEffect(scrollDelta) {
        scrollState.dispatchRawDelta(-scrollDelta)
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                logDebug<Screen.Epg> { "onPostScroll($consumed, $available, $source)" }
                onScrollDeltaChange(consumed.y)
                return super.onPostScroll(consumed, available, source)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .nestedScroll(nestedScrollConnection),
    ) {
        item {
            Spacer(modifier = Modifier.height(HeaderHeight))
        }

        items(
            items = nextMonth,
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
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EpgChannelEntry(
    modifier: Modifier = Modifier,
    user: User,
    segments: LazyPagingItems<ChannelScheduleSegment>,
    currentTime: Instant = Clock.System.now(),
    scrollDelta: Float = 0f,
    onScrollDeltaChange: (Float) -> Unit = {},
) {
    val scrollState = rememberLazyListState()

    LaunchedEffect(scrollDelta) {
        logDebug<Screen.Epg> { "LaunchedEffect: $scrollDelta" }
        scrollState.dispatchRawDelta(-scrollDelta)
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                onScrollDeltaChange(consumed.y)
                return super.onPostScroll(consumed, available, source)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .nestedScroll(nestedScrollConnection),
        state = scrollState,
    ) {
        stickyHeader(
            key = user.login,
            contentType = "user",
        ) {
            Column(
                modifier = Modifier
                    .height(HeaderHeight)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AsyncImage(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surface),
                    model = remoteImageModel(user.profileImageUrl),
                    contentDescription = null,
                )

                Text(
                    user.displayName,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        items(
            count = segments.itemCount,
            key = { index -> segments[index]?.id ?: index },
            contentType = { "segment" },
        ) { index ->
            val item = segments[index]

            if (item == null) {
                Spacer(modifier = Modifier.height(48.dp))
            } else {
                val previousEndTime: Instant =
                    if (index > 0) {
                        segments[index - 1]?.endTime ?: currentTime
                    } else {
                        currentTime
                    }

                Spacer(
                    modifier = Modifier
                        .height(heightForDuration(item.startTime - previousEndTime))
                        .fillMaxWidth(),
                )

                EpgSegment(
                    modifier = Modifier
                        .height(heightForDuration(item.endTime - item.startTime))
                        .fillMaxWidth(),
                    segment = item,
                )
            }
        }
    }
}

@Composable
private fun EpgSegment(
    modifier: Modifier = Modifier,
    segment: ChannelScheduleSegment,
) {
    Card(modifier = modifier) {
        Text(segment.title)
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
        )
    }
}

@Preview
@Composable
private fun EpgChannelPreview() {
    AppTheme {
        val items = flowOf(
            PagingData.from(
                listOf(
                    ChannelScheduleSegment(
                        id = "1",
                        title = "Title",
                        startTime = Instant.parse("2022-01-01T12:00:00Z"),
                        endTime = Instant.parse("2022-01-01T13:00:00Z"),
                        category = null,
                        isRecurring = false,
                    ),
                    ChannelScheduleSegment(
                        id = "2",
                        title = "Title",
                        startTime = Instant.parse("2022-01-01T13:00:00Z"),
                        endTime = Instant.parse("2022-01-01T14:00:00Z"),
                        category = null,
                        isRecurring = false,
                    ),
                ),
            ),
        )

        EpgChannelEntry(
            user = User(
                id = "1",
                login = "login",
                displayName = "Display Name",
                profileImageUrl = "https://example.com/image.jpg",
            ),
            segments = items.collectAsLazyPagingItems(),
        )
    }
}

private fun heightForDuration(duration: Duration): Dp {
    return duration.toDouble(DurationUnit.HOURS) * HourHeight
}

private val HourHeight = 15.dp
private val HeaderHeight = 100.dp
private val ColumnWidth = 200.dp
