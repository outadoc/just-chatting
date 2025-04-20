package fr.outadoc.justchatting.feature.emotes.data.stv.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class StvEmoteHost(
    @SerialName("url")
    val baseUrl: String,
    @SerialName("files")
    val files: List<StvEmoteFiles>,
)
