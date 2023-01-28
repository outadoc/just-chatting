package fr.outadoc.justchatting.feature.chat.presentation

import kotlinx.datetime.Instant
import kotlin.time.Duration

data class MessagePostConstraint(
    val lastMessageSentAt: Instant,
    val slowModeDuration: Duration,
)
