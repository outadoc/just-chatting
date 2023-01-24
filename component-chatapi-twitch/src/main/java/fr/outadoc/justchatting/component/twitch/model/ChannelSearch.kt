package fr.outadoc.justchatting.component.twitch.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChannelSearch(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("broadcaster_login")
    val broadcasterLogin: String? = null,
    @SerializedName("display_name")
    val broadcasterDisplayName: String? = null,
    @SerializedName("broadcaster_language")
    val broadcasterLanguage: String? = null,
    @SerializedName("game_id")
    val gameId: String? = null,
    @SerializedName("game_name")
    val gameName: String? = null,
    @SerializedName("is_live")
    val isLive: Boolean = false,
    @SerializedName("started_at")
    val startedAt: String? = null,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,

    val profileImageURL: String? = null
) : Parcelable
