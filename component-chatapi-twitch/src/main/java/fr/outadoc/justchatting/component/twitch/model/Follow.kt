package fr.outadoc.justchatting.component.twitch.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Follow(
    @SerialName("from_id")
    val fromId: String? = null,
    @SerialName("from_login")
    val fromLogin: String? = null,
    @SerialName("from_name")
    val fromName: String? = null,
    @SerialName("to_id")
    val toId: String? = null,
    @SerialName("to_login")
    val toLogin: String? = null,
    @SerialName("to_name")
    val toName: String? = null,
    @SerialName("followed_at")
    val followedAt: String? = null,
) : Parcelable
