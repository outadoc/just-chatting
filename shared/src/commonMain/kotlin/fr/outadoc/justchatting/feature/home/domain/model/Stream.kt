package fr.outadoc.justchatting.feature.home.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.Instant

@Immutable
internal data class Stream(
    val id: String,
    val userId: String,
    val category: StreamCategory?,
    val title: String,
    val viewerCount: Long,
    val startedAt: Instant,
    val tags: ImmutableSet<String> = persistentSetOf(),
)
