package fr.outadoc.justchatting.component.twitch.http.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StvEmoteResponse(
    @SerialName("emotes")
    val emotes: List<StvEmote>,
)
