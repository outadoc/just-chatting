package fr.outadoc.justchatting.feature.home.data.model

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChannelScheduleVacation(
    @SerialName("start_time")
    @Serializable(with = InstantIso8601Serializer::class)
    val startTime: Instant,
    @SerialName("end_time")
    @Serializable(with = InstantIso8601Serializer::class)
    val endTime: Instant,
)
