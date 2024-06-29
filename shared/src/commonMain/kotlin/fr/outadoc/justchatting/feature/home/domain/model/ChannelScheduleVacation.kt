package fr.outadoc.justchatting.feature.home.domain.model

import kotlinx.datetime.Instant

internal data class ChannelScheduleVacation(
    val startTime: Instant,
    val endTime: Instant,
)