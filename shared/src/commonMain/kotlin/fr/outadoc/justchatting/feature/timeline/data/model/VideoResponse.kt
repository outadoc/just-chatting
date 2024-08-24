package fr.outadoc.justchatting.feature.timeline.data.model

import fr.outadoc.justchatting.feature.shared.data.model.Pagination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class VideoResponse(
    @SerialName("data")
    val data: List<Video>,
    @SerialName("pagination")
    val pagination: Pagination,
)
