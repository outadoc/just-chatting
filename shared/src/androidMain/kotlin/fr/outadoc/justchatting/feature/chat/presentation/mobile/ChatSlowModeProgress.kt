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
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.chat.presentation.MessagePostConstraint
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

@Composable
fun ChatSlowModeProgress(
    modifier: Modifier,
    state: ChatViewModel.State,
) {
    when (state) {
        is ChatViewModel.State.Initial -> {}
        is ChatViewModel.State.Failed -> {}
        is ChatViewModel.State.Chatting -> {
            ChatSlowModeProgress(
                modifier = modifier,
                constraint = state.messagePostConstraint
                    ?: MessagePostConstraint(
                        lastMessageSentAt = Instant.DISTANT_PAST,
                        slowModeDuration = Duration.ZERO,
                    ),
            )
        }
    }
}

@Composable
fun ChatSlowModeProgress(
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

    val progressVisibility = animateFloatAsState(targetValue = if (progress.isRunning) 1f else 0f)

    LinearProgressIndicator(
        modifier = modifier.alpha(progressVisibility.value),
        progress = progress.value,
    )
}
