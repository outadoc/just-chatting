package fr.outadoc.justchatting.feature.home.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable
internal data class FullSchedule(
    val past: Map<LocalDate, List<ChannelScheduleSegment>>,
    val live: List<Stream>,
    val future: Map<LocalDate, List<ChannelScheduleSegment>>,
    val initialListIndex: Int,
)
