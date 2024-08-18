package fr.outadoc.justchatting.feature.home.data.model

import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
internal data class Stream(
    @SerialName("id")
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("user_login")
    val userLogin: String,
    @SerialName("user_name")
    val userName: String,
    @SerialName("game_id")
    val gameId: String? = null,
    @SerialName("game_name")
    val gameName: String? = null,
    @SerialName("title")
    val title: String,
    @SerialName("viewer_count")
    val viewerCount: Long,
    @SerialName("started_at")
    val startedAt: String,
    @SerialName("tags")
    val tags: List<String> = emptyList(),
) : Parcelable
