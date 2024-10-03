package fr.outadoc.justchatting.feature.timeline.presentation.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream

@Composable
internal fun LiveTimelineSegment(
    modifier: Modifier = Modifier,
    userStream: UserStream,
    onChannelClick: (User) -> Unit = {},
    onOpenInBubble: (User) -> Unit = {},
) {
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
        LiveDetailsDialog(
            user = userStream.user,
            stream = userStream.stream,
            onDismissRequest = { isExpanded = false },
            onChannelClick = onChannelClick,
            onOpenInBubble = onOpenInBubble,
        )
    }
}
