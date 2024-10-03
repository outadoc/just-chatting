package fr.outadoc.justchatting.feature.timeline.presentation.mobile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.chat.presentation.mobile.StreamInfo
import fr.outadoc.justchatting.feature.chat.presentation.mobile.UserInfo
import fr.outadoc.justchatting.feature.details.presentation.DetailsDialog
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.core.createChannelExternalLink

@Composable
internal fun LiveTimelineSegment(
    modifier: Modifier = Modifier,
    userStream: UserStream,
    onChannelClick: (User) -> Unit = {},
    onOpenInBubble: (User) -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    var isExpanded by remember { mutableStateOf(false) }

    LiveStreamCard(
        modifier = modifier,
        title = userStream.stream.title,
        userName = userStream.user.displayName,
        viewerCount = userStream.stream.viewerCount,
        category = userStream.stream.category,
        startedAt = userStream.stream.startedAt,
        tags = userStream.stream.tags,
        profileImageUrl = userStream.user.profileImageUrl,
        onClick = {
            onChannelClick(userStream.user)
        },
        onLongClick = {
            isExpanded = true
        },
    )

    if (isExpanded) {
        DetailsDialog(
            onDismissRequest = { isExpanded = false },
            userDetails = {
                UserInfo(user = userStream.user)
            },
            streamDetails = {
                StreamInfo(stream = userStream.stream)
            },
            actions = {
                item {
                    ContextualButton(
                        onClick = {
                            onChannelClick(userStream.user)
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
                            onOpenInBubble(userStream.user)
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
                                createChannelExternalLink(userStream.user),
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
