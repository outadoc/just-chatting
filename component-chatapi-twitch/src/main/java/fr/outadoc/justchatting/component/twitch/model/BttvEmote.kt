package fr.outadoc.justchatting.component.twitch.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BttvEmote(
    @SerialName("id")
    val id: String,
    @SerialName("code")
    val code: String,
    @SerialName("imageType")
    val imageType: String,
    @SerialName("animated")
    val animated: Boolean
)
