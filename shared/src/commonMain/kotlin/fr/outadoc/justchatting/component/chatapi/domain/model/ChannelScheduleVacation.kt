package fr.outadoc.justchatting.component.chatapi.domain.model

import kotlinx.datetime.Instant

internal data class ChannelScheduleVacation(
    val startTime: Instant,
    val endTime: Instant,
)
