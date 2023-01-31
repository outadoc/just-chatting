package fr.outadoc.justchatting.component.twitch.http.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Stream(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("user_login")
    val userLogin: String? = null,
    @SerialName("user_name")
    val userName: String? = null,
    @SerialName("game_id")
    val gameId: String? = null,
    @SerialName("game_name")
    val gameName: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("viewer_count")
    val viewerCount: Int? = null,
    @SerialName("started_at")
    val startedAt: String? = null,
) : Parcelable
