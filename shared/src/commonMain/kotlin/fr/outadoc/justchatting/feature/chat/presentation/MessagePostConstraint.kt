package fr.outadoc.justchatting.feature.chat.presentation

import androidx.compose.runtime.Immutable
import kotlin.time.Duration
import kotlin.time.Instant

@Immutable
internal data class MessagePostConstraint(
    val lastMessageSentAt: Instant = Instant.DISTANT_PAST,
    val slowModeDuration: Duration = Duration.ZERO,
)
