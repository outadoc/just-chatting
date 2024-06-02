package fr.outadoc.justchatting.component.chatapi.domain.model

import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize
import kotlinx.collections.immutable.persistentListOf

@Parcelize
data class ChannelSearch(
    val id: String,
    val title: String,
    val broadcasterLogin: String,
    val broadcasterDisplayName: String,
    val broadcasterLanguage: String? = null,
    val gameId: String? = null,
    val gameName: String? = null,
    val isLive: Boolean = false,
    val startedAt: String? = null,
    val thumbnailUrl: String? = null,
    val profileImageUrl: String? = null,
    val tags: List<String> = persistentListOf(),
) : Parcelable
