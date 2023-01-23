package fr.outadoc.justchatting.component.chatapi.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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
    val profileImageURL: String? = null
) : Parcelable
