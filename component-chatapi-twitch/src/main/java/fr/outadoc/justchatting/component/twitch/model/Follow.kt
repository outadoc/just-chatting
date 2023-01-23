package fr.outadoc.justchatting.component.twitch.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Follow(
    @SerializedName("from_id")
    val fromId: String? = null,
    @SerializedName("from_login")
    val fromLogin: String? = null,
    @SerializedName("from_name")
    val fromName: String? = null,
    @SerializedName("to_id")
    val toId: String? = null,
    @SerializedName("to_login")
    val toLogin: String? = null,
    @SerializedName("to_name")
    val toName: String? = null,
    @SerializedName("followed_at")
    val followedAt: String? = null,

    val profileImageURL: String? = null
) : Parcelable
