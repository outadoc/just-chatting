package fr.outadoc.justchatting.feature.timeline.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Instant

@Immutable
public data class Stream(
    val id: String,
    val userId: String,
    val category: StreamCategory?,
    val title: String,
    val viewerCount: Long,
    val startedAt: Instant,
    val tags: ImmutableList<String> = persistentListOf(),
)
