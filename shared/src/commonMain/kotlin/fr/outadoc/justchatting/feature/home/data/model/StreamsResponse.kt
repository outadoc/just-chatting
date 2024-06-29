package fr.outadoc.justchatting.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class StreamsResponse(
    @SerialName("data")
    val data: List<Stream>,
    @SerialName("pagination")
    val pagination: Pagination,
)
