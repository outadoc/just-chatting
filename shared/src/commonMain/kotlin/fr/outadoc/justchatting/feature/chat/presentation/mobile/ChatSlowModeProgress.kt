package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import fr.outadoc.justchatting.feature.chat.presentation.MessagePostConstraint
import kotlin.time.Clock

@Composable
internal fun ChatSlowModeProgress(
    modifier: Modifier,
    constraint: MessagePostConstraint,
) {
    val slowModeDuration = constraint.slowModeDuration
    val progress = remember(slowModeDuration) { Animatable(initialValue = 1f) }

    LaunchedEffect(constraint) {
        val now = Clock.System.now()
        val durationSinceLastMessage = now - constraint.lastMessageSentAt

        if (durationSinceLastMessage > slowModeDuration) {
            progress.snapTo(0f)
        } else {
            val remainingSlowModeDuration = slowModeDuration - durationSinceLastMessage

            progress.snapTo(
                targetValue = remainingSlowModeDuration.inWholeMilliseconds.toFloat() /
                    slowModeDuration.inWholeMilliseconds.toFloat(),
            )

            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = remainingSlowModeDuration.inWholeMilliseconds.toInt(),
                    easing = LinearEasing,
                ),
            )
        }
    }

    val progressVisibility = animateFloatAsState(
        targetValue = if (progress.isRunning) 1f else 0f,
        label = "Slow mode progress countdown",
    )

    LinearProgressIndicator(
        modifier = modifier.alpha(progressVisibility.value),
        progress = { progress.value },
        strokeCap = StrokeCap.Square,
        drawStopIndicator = {},
    )
}
