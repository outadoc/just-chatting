package fr.outadoc.justchatting.feature.chat.presentation

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlin.time.Duration

@Immutable
internal data class MessagePostConstraint(
    val lastMessageSentAt: Instant = Instant.DISTANT_PAST,
    val slowModeDuration: Duration = Duration.ZERO,
)
