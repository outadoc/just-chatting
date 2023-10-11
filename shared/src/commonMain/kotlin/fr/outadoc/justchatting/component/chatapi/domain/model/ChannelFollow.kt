package fr.outadoc.justchatting.component.chatapi.domain.model

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize

@Parcelize
data class ChannelFollow(
    val userId: String? = null,
    val userLogin: String? = null,
    val userDisplayName: String? = null,
    val followedAt: String? = null,
    val profileImageURL: String? = null,
) : Parcelable
