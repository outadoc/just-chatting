package fr.outadoc.justchatting.component.chatapi.domain.model

import dev.icerock.moko.parcelize.Parcelable
import kotlinx.collections.immutable.persistentListOf
import dev.icerock.moko.parcelize.Parcelize

@Parcelize
data class ChannelSearch(
    val id: String? = null,
    val title: String? = null,
    val broadcasterLogin: String? = null,
    val broadcasterDisplayName: String? = null,
    val broadcasterLanguage: String? = null,
    val gameId: String? = null,
    val gameName: String? = null,
    val isLive: Boolean = false,
    val startedAt: String? = null,
    val thumbnailUrl: String? = null,
    val profileImageURL: String? = null,
    val tags: List<String> = persistentListOf(),
) : Parcelable
