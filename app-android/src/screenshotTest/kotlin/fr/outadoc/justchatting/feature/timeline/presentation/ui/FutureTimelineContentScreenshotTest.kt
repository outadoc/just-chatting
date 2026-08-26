package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.DaySchedule
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
        FutureTimelineContent(
            future =
                persistentListOf(
                    DaySchedule(
                        date = JCLocalDate(LocalDate(2022, 1, 1)),
                        schedule =
                            listOf(
                                ChannelScheduleSegment(
                                    id = "1",
                                    user = PreviewFixtures.sampleUser,
                                    title = PreviewFixtures.sampleTextShort,
                                    startTime = Instant.parse("2022-01-01T12:00:00Z"),
                                    endTime = Instant.parse("2022-01-01T13:00:00Z"),
                                    category = PreviewFixtures.sampleStreamCategory,
                                ),
                            ),
                    ),
                ),
            isRefreshing = false,
            onRefresh = {},
            showRefreshIndicator = false,
            listState = rememberLazyListState(),
        )
    }
}
