package fr.outadoc.justchatting.feature.emotes.data.stv.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class StvEmote(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("flags")
    val flags: Int,
    @SerialName("data")
    val data: StvEmoteData,
)
