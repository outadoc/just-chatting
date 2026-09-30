package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.presentation.ui.StreamInfoRow
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.formatDate
import fr.outadoc.justchatting.utils.presentation.formatHourMinute
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

@Composable
public fun TimelineSegmentDetails(
    modifier: Modifier = Modifier,
    segment: ChannelScheduleSegment,
    timeZone: TimeZone,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (segment.title.isNotEmpty()) {
                Text(
                    text = segment.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 5,
                )
            }

            StreamInfoRow(
                icon = Icons.Outlined.CalendarToday,
                text = segment.startTime.formatDate(tz = timeZone),
            )

            StreamInfoRow(
                icon = Icons.Outlined.Schedule,
                text =
                    buildString {
                        append(segment.startTime.formatHourMinute(timeZone).orEmpty())

                        segment.endTime?.let { endTime ->
                            append(" – ")
                            append(endTime.formatHourMinute(timeZone).orEmpty())
                        }
                    },
            )

            segment.category?.let { category ->
                StreamInfoRow(
                    icon = Icons.Outlined.SportsEsports,
                    text = category.name,
                )
            }
        }
    }
}

@Preview
@Composable
private fun TimelineSegmentDetailsPreview() {
    val lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
    AppTheme {
        TimelineSegmentDetails(
            timeZone = TimeZone.UTC,
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
