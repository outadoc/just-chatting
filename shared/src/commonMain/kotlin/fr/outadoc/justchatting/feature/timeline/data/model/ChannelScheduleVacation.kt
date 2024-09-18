package fr.outadoc.justchatting.feature.timeline.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChannelScheduleVacation(
    @SerialName("start_time")
    val startTimeIso: String,
    @SerialName("end_time")
    val endTimeIso: String,
)
