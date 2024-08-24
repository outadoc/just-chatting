package fr.outadoc.justchatting.feature.timeline.data.model

import fr.outadoc.justchatting.feature.shared.data.model.Pagination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class StreamsResponse(
    @SerialName("data")
    val data: List<Stream>,
    @SerialName("pagination")
    val pagination: Pagination,
)
