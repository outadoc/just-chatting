package fr.outadoc.justchatting.feature.timeline.data.model

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChannelScheduleSegment(
    @SerialName("id")
    val id: String,
    @SerialName("start_time")
    @Serializable(with = InstantIso8601Serializer::class)
    val startTime: Instant,
    @SerialName("end_time")
    @Serializable(with = InstantIso8601Serializer::class)
    val endTime: Instant,
    @SerialName("title")
    val title: String,
    @SerialName("canceled_until")
    @Serializable(with = InstantIso8601Serializer::class)
    val canceledUntil: Instant? = null,
    @SerialName("category")
    val category: StreamCategory? = null,
    @SerialName("is_recurring")
    val isRecurring: Boolean,
)
