package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlin.time.Instant

@PreviewTest
@Preview
@Composable
internal fun TimelineSegmentDetailsScreenshotTest() {
    val lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
    AppTheme {
        TimelineSegmentDetails(
            segment =
                ChannelScheduleSegment(
                    id = "1",
                    user =
                        User(
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
                    category =
                        StreamCategory(
                            id = "1",
                            name = lorem,
                        ),
                ),
        )
    }
}
