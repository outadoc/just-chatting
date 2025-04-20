package fr.outadoc.justchatting.feature.emotes.data.stv.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class StvChannelResponse(
    @SerialName("emote_set")
    val emoteSet: StvEmoteResponse,
)
