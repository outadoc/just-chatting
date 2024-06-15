package fr.outadoc.justchatting.component.twitch.http.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChannelScheduleResponse(
    @SerialName("data")
    val data: List<ChannelSchedule>,
    @SerialName("pagination")
    val pagination: Pagination,
)
