package fr.outadoc.justchatting.component.chatapi.domain.model

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize

@Parcelize
data class ChannelFollow(
    val userId: String,
    val userLogin: String,
    val userDisplayName: String,
    val followedAt: String,
    val profileImageURL: String? = null,
) : Parcelable
