package fr.outadoc.justchatting.feature.timeline.domain.model

import fr.outadoc.justchatting.utils.datetime.JCLocalDate

public data class DaySchedule(
    val date: JCLocalDate,
    val schedule: List<ChannelScheduleSegment>,
)
