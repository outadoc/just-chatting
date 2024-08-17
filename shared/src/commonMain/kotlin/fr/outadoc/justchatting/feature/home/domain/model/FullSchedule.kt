package fr.outadoc.justchatting.feature.home.domain.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class FullSchedule(
    val past: List<ChannelScheduleSegment>,
    val live: List<ChannelScheduleSegment>,
    val future: List<ChannelScheduleSegment>,
)
