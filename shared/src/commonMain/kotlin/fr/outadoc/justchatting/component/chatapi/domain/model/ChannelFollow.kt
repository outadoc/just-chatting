package fr.outadoc.justchatting.component.chatapi.domain.model

import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize

@Parcelize
internal data class ChannelFollow(
    val user: User,
    val followedAt: String,
) : Parcelable
