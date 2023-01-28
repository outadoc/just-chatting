package fr.outadoc.justchatting.component.twitch.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmoteSetResponse(
    @SerialName("template")
    val template: String,
    @SerialName("data")
    val data: List<TwitchEmote>,
)
