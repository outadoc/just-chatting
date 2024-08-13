package fr.outadoc.justchatting.feature.home.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class Stream(
    val id: String,
    val user: User,
    val gameName: String? = null,
    val title: String,
    val viewerCount: Int,
    val startedAt: String,
    val tags: ImmutableList<String> = persistentListOf(),
)
