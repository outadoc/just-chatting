package fr.outadoc.justchatting.component.twitch.http.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CheerEmotesResponse(
    @SerialName("data")
    val data: List<CheerEmote>,
)
