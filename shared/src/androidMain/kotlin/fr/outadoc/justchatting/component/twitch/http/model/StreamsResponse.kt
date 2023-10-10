package fr.outadoc.justchatting.component.twitch.http.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamsResponse(
    @SerialName("data")
    val data: List<Stream>,
    @SerialName("pagination")
    val pagination: Pagination,
)
