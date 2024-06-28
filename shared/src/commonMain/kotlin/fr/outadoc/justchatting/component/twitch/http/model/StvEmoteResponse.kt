package fr.outadoc.justchatting.component.twitch.http.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class StvEmoteResponse(
    @SerialName("emotes")
    val emotes: List<StvEmote>,
)
