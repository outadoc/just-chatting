package fr.outadoc.justchatting.component.twitch.http.model

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class User(
    @SerialName("id")
    val id: String,
    @SerialName("login")
    val login: String,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("description")
    val description: String,
    @SerialName("profile_image_url")
    val profileImageUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String,
) : Parcelable
