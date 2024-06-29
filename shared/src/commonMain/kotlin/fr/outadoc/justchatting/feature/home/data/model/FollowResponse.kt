package fr.outadoc.justchatting.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FollowResponse(
    @SerialName("total")
    val total: Int,
    @SerialName("data")
    val data: List<ChannelFollow>,
    @SerialName("pagination")
    val pagination: Pagination,
)
