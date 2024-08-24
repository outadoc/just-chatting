package fr.outadoc.justchatting.feature.timeline.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChannelSchedule(
    @SerialName("segments")
    val segments: List<ChannelScheduleSegment>?,
    @SerialName("broadcaster_id")
    val userId: String,
    @SerialName("broadcaster_login")
    val userLogin: String,
    @SerialName("broadcaster_name")
    val userDisplayName: String,
    @SerialName("vacation")
    val vacation: ChannelScheduleVacation? = null,
)
