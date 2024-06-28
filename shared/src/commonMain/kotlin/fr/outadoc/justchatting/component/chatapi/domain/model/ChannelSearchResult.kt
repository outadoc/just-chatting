package fr.outadoc.justchatting.component.chatapi.domain.model

import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize
import kotlinx.collections.immutable.persistentListOf

@Parcelize
internal data class ChannelSearchResult(
    val title: String,
    val user: User,
    val language: String? = null,
    val gameId: String? = null,
    val gameName: String? = null,
    val isLive: Boolean = false,
    val thumbnailUrl: String? = null,
    val tags: List<String> = persistentListOf(),
) : Parcelable
