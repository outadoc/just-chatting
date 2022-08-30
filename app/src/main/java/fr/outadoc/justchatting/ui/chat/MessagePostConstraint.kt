package fr.outadoc.justchatting.ui.chat

import kotlinx.datetime.Instant
import kotlin.time.Duration

data class MessagePostConstraint(
    val lastMessageSentAt: Instant,
    val slowModeDuration: Duration
)
