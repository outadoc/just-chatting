package fr.outadoc.justchatting.feature.timeline.data.model

import fr.outadoc.justchatting.feature.shared.data.model.Pagination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChannelScheduleResponse(
    @SerialName("data")
    val data: ChannelSchedule,
    @SerialName("pagination")
    val pagination: Pagination,
)
