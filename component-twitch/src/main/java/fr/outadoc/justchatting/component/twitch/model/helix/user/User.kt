package fr.outadoc.justchatting.component.twitch.model.helix.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @SerializedName("id")
    val id: String,
    @SerializedName("login")
    val login: String,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null,
    @SerializedName("offline_image_url")
    val offlineImageUrl: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("followers_count")
    val followersCount: Int? = null
) : Parcelable
