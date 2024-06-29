package fr.outadoc.justchatting.feature.home.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChannelScheduleResponse(
    @SerialName("data")
    val data: ChannelSchedule,
    @SerialName("pagination")
    val pagination: Pagination,
)
