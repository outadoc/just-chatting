package fr.outadoc.justchatting.feature.home.domain.model

internal data class ChannelSchedule(
    val segments: List<ChannelScheduleSegment>,
    val user: User,
    val vacation: ChannelScheduleVacation? = null,
)
