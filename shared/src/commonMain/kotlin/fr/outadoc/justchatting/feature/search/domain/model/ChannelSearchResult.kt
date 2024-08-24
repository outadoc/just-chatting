package fr.outadoc.justchatting.feature.search.domain.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class ChannelSearchResult(
    val title: String,
    val user: User,
    val language: String? = null,
    val gameId: String? = null,
    val gameName: String? = null,
    val isLive: Boolean = false,
    val thumbnailUrl: String? = null,
    val tags: ImmutableList<String> = persistentListOf(),
)
