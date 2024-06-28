package fr.outadoc.justchatting.component.chatapi.domain.model

import kotlinx.datetime.Instant

internal data class RecentEmote(
    val name: String,
    val url: String,
    val usedAt: Instant,
)
