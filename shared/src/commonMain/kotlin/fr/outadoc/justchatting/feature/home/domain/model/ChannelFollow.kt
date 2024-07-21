package fr.outadoc.justchatting.feature.home.domain.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize

@Immutable
@Parcelize
internal data class ChannelFollow(
    val user: User,
    val followedAt: String,
) : Parcelable
