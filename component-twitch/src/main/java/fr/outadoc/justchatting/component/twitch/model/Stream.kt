package fr.outadoc.justchatting.component.twitch.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Stream(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("user_id")
    val userId: String? = null,
    @SerializedName("user_login")
    val userLogin: String? = null,
    @SerializedName("user_name")
    val userName: String? = null,
    @SerializedName("game_id")
    val gameId: String? = null,
    @SerializedName("game_name")
    val gameName: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("viewer_count")
    val viewerCount: Int? = null,
    @SerializedName("started_at")
    val startedAt: String? = null,

    val profileImageURL: String? = null
) : Parcelable
