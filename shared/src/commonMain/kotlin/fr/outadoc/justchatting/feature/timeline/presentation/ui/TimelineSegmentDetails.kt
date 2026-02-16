package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.utils.presentation.AppTheme
import fr.outadoc.justchatting.utils.presentation.formatDate
import fr.outadoc.justchatting.utils.presentation.formatHourMinute
import kotlin.time.Instant

@Composable
internal fun TimelineSegmentDetails(
    modifier: Modifier = Modifier,
    segment: ChannelScheduleSegment,
) {
    Column(
        modifier = modifier,
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
            segment.startTime.formatDate()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier =
                Modifier
                    .size(24.dp)
                    .padding(end = 8.dp),
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
            )

            Text(date)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier =
                Modifier
                    .size(24.dp)
                    .padding(end = 8.dp),
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
            )

            Text(
                buildAnnotatedString {
                    append(segment.startTime.formatHourMinute())

                    if (segment.endTime != null) {
                        append(" - ")
                        append(segment.endTime.formatHourMinute())
                    }
                },
            )
        }

        segment.category?.let { category ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier =
                    Modifier
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

@Preview
@Composable
private fun TimelineSegmentDetailsPreview() {
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
