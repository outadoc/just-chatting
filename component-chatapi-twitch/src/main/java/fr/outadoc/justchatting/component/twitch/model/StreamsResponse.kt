package fr.outadoc.justchatting.component.twitch.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamsResponse(
    @SerialName("data")
    val data: List<Stream>?,
    @SerialName("pagination")
    val pagination: Pagination?,
)
