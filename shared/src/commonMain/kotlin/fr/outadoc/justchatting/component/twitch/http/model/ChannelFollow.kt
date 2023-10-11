package fr.outadoc.justchatting.component.twitch.http.model

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ChannelFollow(
    @SerialName("broadcaster_id")
    val userId: String? = null,
    @SerialName("broadcaster_login")
    val userLogin: String? = null,
    @SerialName("broadcaster_name")
    val userDisplayName: String? = null,
    @SerialName("followed_at")
    val followedAt: String? = null,
) : Parcelable
