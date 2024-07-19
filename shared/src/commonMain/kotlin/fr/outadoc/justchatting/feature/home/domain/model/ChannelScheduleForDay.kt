package fr.outadoc.justchatting.feature.home.domain.model

import kotlinx.datetime.LocalDate

internal data class ChannelScheduleForDay(
    val date: LocalDate,
    val segments: List<ChannelScheduleSegment>,
)
