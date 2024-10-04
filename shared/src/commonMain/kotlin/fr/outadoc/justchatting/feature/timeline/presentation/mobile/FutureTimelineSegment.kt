package fr.outadoc.justchatting.feature.timeline.presentation.mobile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.presentation.mobile.BasicUserInfo
import fr.outadoc.justchatting.feature.details.presentation.ActionBottomSheet
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.utils.presentation.format
import fr.outadoc.justchatting.utils.presentation.formatHourMinute

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun FutureTimelineSegment(
    modifier: Modifier = Modifier,
    segment: ChannelScheduleSegment,
    onUserClick: () -> Unit = {},
) {
    var isExpanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier,
    ) {
        Column {
            Card(
                modifier = Modifier
                    .combinedClickable(
                        onClick = { isExpanded = true },
                        onLongClick = { isExpanded = true },
                    ),
            ) {
                TimelineSegmentContent(
                    modifier = Modifier.padding(8.dp),
                    title = segment.title,
                    userName = segment.user.displayName,
                    category = segment.category,
                    profileImageUrl = segment.user.profileImageUrl,
                    onUserClick = onUserClick,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                )

                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = buildAnnotatedString {
                        append(segment.startTime.formatHourMinute())

                        if (segment.endTime != null) {
                            append(" - ")
                            append(segment.endTime.formatHourMinute())
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(
                    modifier = Modifier.weight(1f, fill = true),
                )

                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Default.Timelapse,
                    contentDescription = null,
                )

                if (segment.endTime != null) {
                    val duration = segment.endTime - segment.startTime
                    Text(
                        modifier = Modifier.alignByBaseline(),
                        text = duration.format(showSeconds = false),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }

    if (isExpanded) {
        ActionBottomSheet(
            onDismissRequest = { isExpanded = false },
            header = {
                BasicUserInfo(user = segment.user)
            },
            content = {
                TimelineSegmentDetails(segment = segment)
            },
        )
    }
}
