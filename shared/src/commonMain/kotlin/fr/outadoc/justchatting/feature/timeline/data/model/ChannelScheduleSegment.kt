package fr.outadoc.justchatting.feature.timeline.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChannelScheduleSegment(
    @SerialName("id")
    val id: String,
    @SerialName("start_time")
    val startTimeIso: String,
    @SerialName("end_time")
    val endTimeIso: String,
    @SerialName("title")
    val title: String,
    @SerialName("canceled_until")
    val canceledUntilIso: String? = null,
    @SerialName("category")
    val category: StreamCategory? = null,
    @SerialName("is_recurring")
    val isRecurring: Boolean,
)
