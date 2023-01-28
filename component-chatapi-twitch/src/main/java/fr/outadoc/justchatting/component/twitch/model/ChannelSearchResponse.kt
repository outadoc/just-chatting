package fr.outadoc.justchatting.component.twitch.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChannelSearchResponse(
    @SerialName("data")
    val data: List<ChannelSearch>?,
    @SerialName("pagination")
    val pagination: Pagination?,
)
