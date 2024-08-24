package fr.outadoc.justchatting.feature.followed.data.model

import fr.outadoc.justchatting.feature.shared.data.model.Pagination
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
