package fr.outadoc.justchatting.component.chatapi.domain.model

import kotlinx.datetime.Instant

data class ChannelScheduleVacation(
    val startTime: Instant,
    val endTime: Instant,
)
