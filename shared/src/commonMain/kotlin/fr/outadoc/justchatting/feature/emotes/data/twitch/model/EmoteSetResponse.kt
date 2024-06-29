package fr.outadoc.justchatting.feature.emotes.data.twitch.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class EmoteSetResponse(
    @SerialName("template")
    val template: String,
    @SerialName("data")
    val data: List<TwitchEmote>,
)
