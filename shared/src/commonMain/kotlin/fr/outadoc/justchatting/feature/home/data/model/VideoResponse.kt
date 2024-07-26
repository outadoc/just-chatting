package fr.outadoc.justchatting.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class VideoResponse(
    @SerialName("data")
    val data: List<Video>,
    @SerialName("pagination")
    val pagination: Pagination,
)
