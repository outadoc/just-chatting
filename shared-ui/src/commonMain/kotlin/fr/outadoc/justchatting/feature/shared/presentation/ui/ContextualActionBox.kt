package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.utils.presentation.rememberHasPointingDevice
import kotlinx.coroutines.launch

@Composable
internal fun ContextualActionBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSwiped: () -> Unit,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    if (rememberHasPointingDevice()) {
        HoverActionBox(
            modifier = modifier,
            enabled = enabled,
            onAction = onSwiped,
            icon = icon,
            content = content,
        )
    } else {
        SwipeActionBox(
            modifier = modifier,
            enabled = enabled,
            onSwiped = onSwiped,
            icon = icon,
            content = content,
        )
    }
}

@Composable
private fun HoverActionBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onAction: () -> Unit,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier.hoverable(interactionSource),
    ) {
        content()

        if (enabled) {
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp),
                visible = isHovered,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                IconButton(
                    onClick = onAction,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    icon()
                }
            }
        }
    }
}

@Composable
private fun SwipeActionBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSwiped: () -> Unit,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val direction: SwipeToDismissBoxValue = SwipeToDismissBoxValue.EndToStart
    val dismissState: SwipeToDismissBoxState = rememberSwipeToDismissBoxState()
    val coroutineScope = rememberCoroutineScope()

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = enabled,
        onDismiss = { value ->
            if (value == direction) {
                onSwiped()
            }
            coroutineScope.launch { dismissState.reset() }
        },
        backgroundContent = {
            val scale by animateFloatAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> 0.75f
                    else -> 1f
                },
                label = "Action icon scale",
            )

            val haptic = LocalHapticFeedback.current
            LaunchedEffect(dismissState.targetValue) {
                if (dismissState.targetValue == direction) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(end = 8.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(
                    modifier =
                        Modifier
                            .scale(scale)
                            .alpha(scale),
                ) {
                    icon()
                }
            }
        },
        content = {
            val elevation by animateDpAsState(
                targetValue =
                    when (dismissState.targetValue) {
                        direction -> 2.dp
                        else -> 0.dp
                    },
                label = "Action content elevation",
            )

            Surface(
                shadowElevation = elevation,
            ) {
                content()
            }
        },
    )
}
