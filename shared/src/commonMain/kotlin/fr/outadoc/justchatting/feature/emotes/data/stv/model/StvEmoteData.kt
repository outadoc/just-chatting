package fr.outadoc.justchatting.feature.emotes.data.stv.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class StvEmoteData(
    @SerialName("animated")
    val animated: Boolean,
    @SerialName("host")
    val host: StvEmoteHost,
)
