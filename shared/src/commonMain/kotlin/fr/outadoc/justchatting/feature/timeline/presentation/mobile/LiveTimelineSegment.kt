package fr.outadoc.justchatting.feature.timeline.presentation.mobile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.shared.presentation.mobile.SwipeActionBox
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream
import fr.outadoc.justchatting.shared.MR

@Composable
internal fun LiveTimelineSegment(
    modifier: Modifier = Modifier,
    userStream: UserStream,
    onUserClick: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenInBubble: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    var showDetailsDialog by remember { mutableStateOf(false) }

    SwipeActionBox(
        onSwiped = { showDetailsDialog = true },
        icon = {
            Icon(
                Icons.Default.MoreHoriz,
                contentDescription = stringResource(MR.strings.stream_info),
            )
        },
    ) {
        LiveStreamCard(
            modifier = modifier,
            title = userStream.stream.title,
            userName = userStream.user.displayName,
            viewerCount = userStream.stream.viewerCount,
            category = userStream.stream.category,
            startedAt = userStream.stream.startedAt,
            tags = userStream.stream.tags,
            profileImageUrl = userStream.user.profileImageUrl,
            onUserClick = onUserClick,
            onClick = onOpenChat,
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showDetailsDialog = true
            },
        )
    }

    if (showDetailsDialog) {
        LiveDetailsDialog(
            user = userStream.user,
            stream = userStream.stream,
            onDismissRequest = { showDetailsDialog = false },
            onOpenChat = onOpenChat,
            onOpenInBubble = onOpenInBubble,
        )
    }
}
