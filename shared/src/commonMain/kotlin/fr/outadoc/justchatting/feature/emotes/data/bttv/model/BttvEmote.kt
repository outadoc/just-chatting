package fr.outadoc.justchatting.feature.emotes.data.bttv.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class BttvEmote(
    @SerialName("id")
    val id: String,
    @SerialName("code")
    val code: String,
    @SerialName("imageType")
    val imageType: String,
    @SerialName("animated")
    val animated: Boolean,
    @SerialName("width")
    val width: Int? = null,
    @SerialName("height")
    val height: Int? = null,
)
