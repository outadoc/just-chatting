package fr.outadoc.justchatting.component.twitch.http.model

import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ChannelFollow(
    @SerialName("broadcaster_id")
    val userId: String,
    @SerialName("broadcaster_login")
    val userLogin: String,
    @SerialName("broadcaster_name")
    val userDisplayName: String,
    @SerialName("followed_at")
    val followedAt: String,
) : Parcelable
