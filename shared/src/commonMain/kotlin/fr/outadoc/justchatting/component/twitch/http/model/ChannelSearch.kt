package fr.outadoc.justchatting.component.twitch.http.model

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ChannelSearch(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("broadcaster_login")
    val userLogin: String,
    @SerialName("display_name")
    val userDisplayName: String,
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
    val thumbnailUrl: String? = null,
    @SerialName("tags")
    val tags: List<String> = emptyList(),
) : Parcelable
