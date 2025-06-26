package fr.outadoc.justchatting.feature.timeline.domain.model

import kotlin.time.Duration
import kotlin.time.Instant

internal data class Video(
    val id: String,
    val streamId: String?,
    val userId: String,
    val title: String,
    val description: String,
    val createdAt: Instant,
    val publishedAt: Instant,
    val videoUrl: String,
    val thumbnailUrl: String,
    val viewCount: Int,
    val duration: Duration,
)
