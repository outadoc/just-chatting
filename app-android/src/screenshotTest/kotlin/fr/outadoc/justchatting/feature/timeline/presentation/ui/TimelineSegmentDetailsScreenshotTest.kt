package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.preview.PreviewFixtures
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlin.time.Instant

@PreviewTest
@Preview
@Composable
internal fun TimelineSegmentDetailsScreenshotTest() {
    AppTheme {
        TimelineSegmentDetails(
            segment =
                ChannelScheduleSegment(
                    id = "1",
                    user =
                        PreviewFixtures.user(
                            id = "1",
                            login = "user",
                            displayName = PreviewFixtures.sampleTextShort,
                        ),
                    title = PreviewFixtures.sampleTextShort,
                    startTime = Instant.parse("2022-01-01T12:00:00Z"),
                    endTime = Instant.parse("2022-01-01T13:00:00Z"),
                    category =
                        StreamCategory(
                            id = "1",
                            name = PreviewFixtures.sampleTextShort,
                        ),
                ),
        )
    }
}
