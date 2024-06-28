package fr.outadoc.justchatting.component.twitch.http.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FfzEmote(
    @SerialName("id")
    val id: Int,
    @SerialName("code")
    val code: String,
    @SerialName("images")
    val images: Map<String, String?>,
    @SerialName("imageType")
    val imageType: String,
    @SerialName("animated")
    val animated: Boolean,
)
