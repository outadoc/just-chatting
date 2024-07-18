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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel
import java.time.format.TextStyle
import java.util.Locale

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

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    MainNavigation(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        sizeClass = sizeClass,
        selectedScreen = Screen.Epg,
        onSelectedTabChange = onNavigate,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(MR.strings.epg_title)) },
                scrollBehavior = scrollBehavior,
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

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
    ) {
        stickyHeader("timeline") {
            Timeline()
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
                    user = item.user,
                    segments = segments,
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
    clock: Clock = Clock.System,
    tz: TimeZone = TimeZone.currentSystemDefault(),
) {
    val today = clock.todayIn(tz)
    val nextMonth = buildList {
        for (i in 0..30) {
            add(today.plus(i, DateTimeUnit.DAY))
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxHeight(),
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
                    .height(DayHeight)
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                Text(
                    date.dayOfWeek.getDisplayName(
                        TextStyle.FULL,
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
) {
    LazyColumn(
        modifier = modifier
            .fillMaxHeight(),
    ) {
        stickyHeader(
            key = user.login,
            contentType = "user",
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
                Spacer(modifier = Modifier.height(DayHeight))
            } else {
                EpgSegment(
                    modifier = Modifier.height(DayHeight),
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
        Column {
            Text("${segment.startTime} ‑ ${segment.endTime}")
            Text(segment.title)
        }
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

private val DayHeight = 100.dp
private val HeaderHeight = 100.dp
