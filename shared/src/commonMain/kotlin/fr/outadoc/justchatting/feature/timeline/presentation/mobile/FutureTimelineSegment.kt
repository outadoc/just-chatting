package fr.outadoc.justchatting.feature.timeline.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Card
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.chat.presentation.mobile.UserInfo
import fr.outadoc.justchatting.feature.details.presentation.DetailsDialog
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.core.createChannelExternalLink
import fr.outadoc.justchatting.utils.presentation.format
import fr.outadoc.justchatting.utils.presentation.formatHourMinute

@Composable
internal fun FutureTimelineSegment(
    modifier: Modifier = Modifier,
    segment: ChannelScheduleSegment,
    onChannelClick: (User) -> Unit = {},
    onOpenInBubble: (User) -> Unit = {},
) {
    var isExpanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier,
    ) {
        Column {
            Card(
                onClick = { isExpanded = true },
            ) {
                TimelineSegmentContent(
                    modifier = Modifier.padding(8.dp),
                    title = segment.title,
                    userName = segment.user.displayName,
                    category = segment.category,
                    profileImageUrl = segment.user.profileImageUrl,
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

    val uriHandler = LocalUriHandler.current

    if (isExpanded) {
        DetailsDialog(
            onDismissRequest = { isExpanded = false },
            userDetails = {
                UserInfo(user = segment.user)
            },
            streamDetails = {
                TimelineSegmentDetails(segment = segment)
            },
            actions = {
                item {
                    ContextualButton(
                        onClick = {
                            onChannelClick(segment.user)
                            isExpanded = false
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = null,
                            )
                        },
                        text = "Open chat",
                    )
                }

                item {
                    ContextualButton(
                        onClick = {
                            onOpenInBubble(segment.user)
                            isExpanded = false
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = null,
                            )
                        },
                        text = "Open in bubble",
                    )
                }

                item {
                    ContextualButton(
                        onClick = {
                            uriHandler.openUri(
                                createChannelExternalLink(segment.user),
                            )
                            isExpanded = false
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = null,
                            )
                        },
                        text = stringResource(MR.strings.watch_live),
                    )
                }
            },
        )
    }
}
