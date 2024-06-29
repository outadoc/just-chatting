package fr.outadoc.justchatting.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChannelSearchResponse(
    @SerialName("data")
    val data: List<ChannelSearch>,
    @SerialName("pagination")
    val pagination: Pagination,
)
