package fr.outadoc.justchatting.feature.home.domain.model

import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize

@Parcelize
internal data class ChannelFollow(
    val user: User,
    val followedAt: String,
) : Parcelable
