package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToReply(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberDismissState(
        confirmValueChange = {
            if (it == DismissValue.DismissedToEnd) onDismiss()
            it != DismissValue.DismissedToEnd
        },
    )

    SwipeToDismiss(
        modifier = modifier,
        state = dismissState,
        directions = if (enabled) setOf(DismissDirection.StartToEnd) else emptySet(),
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
            if (direction != DismissDirection.StartToEnd) return@SwipeToDismiss

            val scale by animateFloatAsState(
                if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f,
            )

            val haptic = LocalHapticFeedback.current
            LaunchedEffect(dismissState.targetValue) {
                if (dismissState.targetValue == DismissValue.DismissedToEnd) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(
                    Icons.Default.Reply,
                    contentDescription = "Reply",
                    modifier = Modifier.scale(scale),
                )
            }
        },
        dismissContent = {
            val elevation = animateDpAsState(
                if (dismissState.dismissDirection != null) 4.dp
                else 0.dp
            )

            Surface(shadowElevation = elevation.value) {
                content()
            }
        },
    )
}
