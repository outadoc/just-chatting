package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@Composable
internal fun SwipeToReply(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    val dismissState: SwipeToDismissBoxState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.StartToEnd) {
                    onDismiss()
                }
                value != SwipeToDismissBoxValue.StartToEnd
            },
        )

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        enableDismissFromStartToEnd = enabled,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            val scale by animateFloatAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> 0.75f
                    else -> 1f
                },
                label = "Reply icon scale",
            )

            val haptic = LocalHapticFeedback.current
            LaunchedEffect(dismissState.targetValue) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
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
                    Icons.AutoMirrored.Filled.Reply,
                    contentDescription = "Reply",
                    modifier = Modifier.scale(scale),
                )
            }
        },
        content = {
            val elevation by animateDpAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> 1.dp
                    else -> 0.dp
                },
                label = "Reply item elevation",
            )

            Surface(
                tonalElevation = elevation,
                shadowElevation = elevation,
            ) {
                content()
            }
        },
    )
}
