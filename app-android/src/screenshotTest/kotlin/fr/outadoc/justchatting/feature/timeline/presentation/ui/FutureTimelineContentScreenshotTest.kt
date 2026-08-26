package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.DaySchedule
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.utils.datetime.JCLocalDate
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

@PreviewTest
@Preview
@Composable
internal fun FutureTimelineContentScreenshotTest() {
    val user =
        User(
            id = "1",
            login = "maghla",
            displayName = "Maghla",
            description = "",
            profileImageUrl = "",
            createdAt = Instant.DISTANT_PAST,
            usedAt = Instant.DISTANT_PAST,
        )

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
                                    user = user,
                                    title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                                    startTime = Instant.parse("2022-01-01T12:00:00Z"),
                                    endTime = Instant.parse("2022-01-01T13:00:00Z"),
                                    category = StreamCategory(id = "1", name = "Powerwash Simulator"),
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
