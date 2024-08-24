package fr.outadoc.justchatting.feature.search.data.model

import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
internal data class ChannelSearch(
    @SerialName("id")
    val userId: String,
    @SerialName("broadcaster_login")
    val userLogin: String,
    @SerialName("display_name")
    val userDisplayName: String,
    @SerialName("title")
    val title: String,
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
