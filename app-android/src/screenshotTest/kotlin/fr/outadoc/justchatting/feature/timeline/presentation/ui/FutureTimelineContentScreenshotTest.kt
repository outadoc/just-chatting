package fr.outadoc.justchatting.feature.timeline.presentation.ui

import android.content.res.Configuration
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.timeline.presentation.OngoingScheduleSegment
import fr.outadoc.justchatting.feature.timeline.presentation.ScheduleDay
import fr.outadoc.justchatting.preview.PreviewFixtures
import fr.outadoc.justchatting.utils.datetime.JCLocalDate
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

@PreviewTest
@Preview
@Composable
internal fun FutureTimelineContentScreenshotTest() {
    AppTheme {
        FutureTimelineContentSample()
    }
}

@PreviewTest
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun FutureTimelineContentDarkScreenshotTest() {
    AppTheme(isDarkTheme = true) {
        FutureTimelineContentSample()
    }
}

@Composable
private fun FutureTimelineContentSample() {
    FutureTimelineContent(
        days =
            persistentListOf(
                ScheduleDay(
                    date = JCLocalDate(LocalDate(2022, 1, 1)),
                    daysFromToday = 0,
                    ongoing =
                        persistentListOf(
                            OngoingScheduleSegment(
                                segment =
                                    ChannelScheduleSegment(
                                        id = "1",
                                        user = PreviewFixtures.sampleUser,
                                        title = PreviewFixtures.sampleTextShort,
                                        startTime = Instant.parse("2022-01-01T15:00:00Z"),
                                        endTime = Instant.parse("2022-01-01T19:00:00Z"),
                                        category = PreviewFixtures.sampleStreamCategory,
                                    ),
                                progress = 0.75f,
                            ),
                            OngoingScheduleSegment(
                                segment =
                                    ChannelScheduleSegment(
                                        id = "2",
                                        user =
                                            PreviewFixtures.user(
                                                id = "2",
                                                login = "hortyunderscore",
                                                displayName = "HortyUnderscore",
                                            ),
                                        title = "Lorem ipsum dolor sit amet",
                                        startTime = Instant.parse("2022-01-01T17:30:00Z"),
                                        endTime = Instant.parse("2022-01-01T21:30:00Z"),
                                        category = StreamCategory(id = "2", name = "Just Chatting"),
                                    ),
                                progress = 0.125f,
                            ),
                        ),
                    upcoming =
                        persistentListOf(
                            ChannelScheduleSegment(
                                id = "3",
                                user =
                                    PreviewFixtures.user(
                                        id = "3",
                                        login = "antoinedaniel",
                                        displayName = "AntoineDaniel",
                                    ),
                                title = "",
                                startTime = Instant.parse("2022-01-01T20:00:00Z"),
                                endTime = Instant.parse("2022-01-01T23:00:00Z"),
                            ),
                        ),
                ),
                ScheduleDay(
                    date = JCLocalDate(LocalDate(2022, 1, 2)),
                    daysFromToday = 1,
                    ongoing = persistentListOf(),
                    upcoming =
                        persistentListOf(
                            ChannelScheduleSegment(
                                id = "4",
                                user = PreviewFixtures.sampleUser,
                                title = PreviewFixtures.sampleTextShort,
                                startTime = Instant.parse("2022-01-02T15:00:00Z"),
                                endTime = Instant.parse("2022-01-02T19:00:00Z"),
                                category = PreviewFixtures.sampleStreamCategory,
                            ),
                        ),
                ),
                ScheduleDay(
                    date = JCLocalDate(LocalDate(2022, 1, 3)),
                    daysFromToday = 2,
                    ongoing = persistentListOf(),
                    upcoming = persistentListOf(),
                ),
            ),
        isRefreshing = false,
        onRefresh = {},
        showRefreshIndicator = false,
        listState = rememberLazyListState(),
    )
}
