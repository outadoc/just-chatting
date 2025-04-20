package fr.outadoc.justchatting.feature.emotes.data.stv.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class StvEmoteFiles(
    @SerialName("name")
    val name: String,
    @SerialName("format")
    val format: String,
    @SerialName("width")
    val width: Int,
    @SerialName("height")
    val height: Int,
)
