package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.details.presentation.ActionBottomSheet
import fr.outadoc.justchatting.feature.shared.presentation.ui.ContextualActionBox
import fr.outadoc.justchatting.feature.shared.presentation.ui.SegmentedListDefaults
import fr.outadoc.justchatting.feature.shared.presentation.ui.SegmentedListItem
import fr.outadoc.justchatting.feature.shared.presentation.ui.UserAvatar
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.stream_info
import fr.outadoc.justchatting.utils.presentation.formatHourMinute
import org.jetbrains.compose.resources.stringResource

/**
 * @param progress If the segment is currently ongoing, how far along it is, from 0 to 1.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FutureTimelineSegment(
    modifier: Modifier = Modifier,
    segment: ChannelScheduleSegment,
    shape: Shape = SegmentedListDefaults.StandaloneShape,
    progress: Float? = null,
    onUserClick: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    var showDetailsDialog by remember { mutableStateOf(false) }

    ContextualActionBox(
        onSwiped = { showDetailsDialog = true },
        icon = {
            Icon(
                Icons.Default.MoreHoriz,
                contentDescription = stringResource(Res.string.stream_info),
            )
        },
    ) {
        SegmentedListItem(
            modifier = modifier,
            shape = shape,
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { showDetailsDialog = true },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showDetailsDialog = true
                            },
                        ).padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(modifier = Modifier.widthIn(min = 48.dp)) {
                    segment.startTime.formatHourMinute()?.let { startTime ->
                        Text(
                            text = startTime,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                    }

                    segment.endTime?.formatHourMinute()?.let { endTime ->
                        Text(
                            text = endTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }

                UserAvatar(
                    modifier = Modifier.clickable(onClick = onUserClick),
                    profileImageUrl = segment.user.profileImageUrl,
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = segment.user.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (segment.title.isNotEmpty()) {
                        Text(
                            text = segment.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    segment.category?.let { category ->
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (progress != null) {
                        LinearProgressIndicator(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                            progress = { progress },
                        )
                    }
                }
            }
        }
    }

    if (showDetailsDialog) {
        ActionBottomSheet(
            onDismissRequest = { showDetailsDialog = false },
            header = {
                ChannelDetailsHeader(user = segment.user)
            },
            content = {
                TimelineSegmentDetails(segment = segment)
            },
        )
    }
}
