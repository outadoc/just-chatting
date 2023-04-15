package fr.outadoc.justchatting.component.twitch.http.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
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
