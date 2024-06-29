package fr.outadoc.justchatting.feature.recent.domain.model

import kotlinx.datetime.Instant

internal data class RecentChannel(
    val id: String,
    val usedAt: Instant,
)
