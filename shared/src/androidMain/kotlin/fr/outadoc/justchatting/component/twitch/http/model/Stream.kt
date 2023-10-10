package fr.outadoc.justchatting.component.twitch.http.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Stream(
    @SerialName("id")
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("user_login")
    val userLogin: String,
    @SerialName("user_name")
    val userName: String,
    @SerialName("game_name")
    val gameName: String? = null,
    @SerialName("title")
    val title: String,
    @SerialName("viewer_count")
    val viewerCount: Int,
    @SerialName("started_at")
    val startedAt: String,
    @SerialName("tags")
    val tags: List<String> = emptyList(),
) : Parcelable
