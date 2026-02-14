package fr.outadoc.justchatting.feature.shared.presentation.mobile

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@Composable
internal fun SwipeActionBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSwiped: () -> Unit,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val direction: SwipeToDismissBoxValue = SwipeToDismissBoxValue.EndToStart
    val dismissState: SwipeToDismissBoxState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == direction) {
                    onSwiped()
                }
                value != direction
            },
        )

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = enabled,
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
