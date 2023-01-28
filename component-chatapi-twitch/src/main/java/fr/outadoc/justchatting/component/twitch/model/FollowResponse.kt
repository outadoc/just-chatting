package fr.outadoc.justchatting.component.twitch.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FollowResponse(
    @SerialName("total")
    val total: Int?,
    @SerialName("data")
    val data: List<Follow>?,
    @SerialName("pagination")
    val pagination: Pagination?,
)
