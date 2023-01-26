package fr.outadoc.justchatting.component.twitch.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ChannelSearch(
    @SerialName("id")
    val id: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("broadcaster_login")
    val broadcasterLogin: String? = null,
    @SerialName("display_name")
    val broadcasterDisplayName: String? = null,
    @SerialName("broadcaster_language")
    val broadcasterLanguage: String? = null,
    @SerialName("game_id")
    val gameId: String? = null,
    @SerialName("game_name")
    val gameName: String? = null,
    @SerialName("is_live")
    val isLive: Boolean = false,
    @SerialName("started_at")
    val startedAt: String? = null,
    @SerialName("thumbnail_url")
    val thumbnailUrl: String? = null
) : Parcelable
