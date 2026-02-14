package fr.outadoc.justchatting.feature.timeline.presentation.mobile

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
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.presentation.mobile.BasicUserInfo
import fr.outadoc.justchatting.feature.details.presentation.ActionBottomSheet
import fr.outadoc.justchatting.feature.shared.presentation.mobile.SwipeActionBox
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.all_showDetails_cd
import fr.outadoc.justchatting.shared.stream_info
import fr.outadoc.justchatting.shared.timeline_openVod_action
import fr.outadoc.justchatting.utils.core.createVideoExternalLink
import fr.outadoc.justchatting.utils.presentation.format
import fr.outadoc.justchatting.utils.presentation.formatHourMinute
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PastTimelineSegment(
    modifier: Modifier = Modifier,
    segment: ChannelScheduleSegment,
    onUserClick: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val uriHandler = LocalUriHandler.current

    var showDetailsDialog by remember { mutableStateOf(false) }

    SwipeActionBox(
        onSwiped = { showDetailsDialog = true },
        icon = {
            Icon(
                Icons.Default.MoreHoriz,
                contentDescription = stringResource(Res.string.stream_info),
            )
        },
    ) {
        OutlinedCard(
            modifier = modifier,
        ) {
            Column {
                Card(
                    modifier =
                        Modifier
                            .combinedClickable(
                                onClick = {
                                    uriHandler.openUri(
                                        createVideoExternalLink(segment.id),
                                    )
                                },
                                onClickLabel = stringResource(Res.string.timeline_openVod_action),
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showDetailsDialog = true
                                },
                                onLongClickLabel = stringResource(Res.string.all_showDetails_cd),
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
                    modifier =
                        Modifier
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
                        text =
                            buildAnnotatedString {
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
    }

    if (showDetailsDialog) {
        ActionBottomSheet(
            onDismissRequest = { showDetailsDialog = false },
            header = {
                BasicUserInfo(user = segment.user)
            },
            content = {
                TimelineSegmentDetails(segment = segment)
            },
            actions = { padding ->
                ContextualButton(
                    contentPadding = padding,
                    onClick = {
                        uriHandler.openUri(
                            createVideoExternalLink(segment.id),
                        )
                        showDetailsDialog = false
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                        )
                    },
                    text = stringResource(Res.string.timeline_openVod_action),
                )
            },
        )
    }
}
