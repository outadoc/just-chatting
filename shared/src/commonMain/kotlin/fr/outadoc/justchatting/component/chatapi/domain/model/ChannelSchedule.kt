package fr.outadoc.justchatting.component.chatapi.domain.model

data class ChannelSchedule(
    val segments: List<ChannelScheduleSegment>,
    val userId: String,
    val userLogin: String,
    val userDisplayName: String,
    val vacation: ChannelScheduleVacation? = null,
)
