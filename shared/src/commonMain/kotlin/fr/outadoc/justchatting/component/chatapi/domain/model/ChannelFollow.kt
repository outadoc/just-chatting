package fr.outadoc.justchatting.component.chatapi.domain.model

import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize

@Parcelize
data class ChannelFollow(
    val userId: String,
    val userLogin: String,
    val userDisplayName: String,
    val followedAt: String,
    val profileImageURL: String? = null,
) : Parcelable
