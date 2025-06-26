package fr.outadoc.justchatting.feature.emotes.domain.model

import kotlin.time.Instant

internal data class RecentEmote(
    val name: String,
    val url: String,
    val usedAt: Instant,
)
