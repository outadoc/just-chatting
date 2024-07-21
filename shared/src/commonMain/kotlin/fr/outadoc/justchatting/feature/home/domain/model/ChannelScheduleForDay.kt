package fr.outadoc.justchatting.feature.home.domain.model

import androidx.compose.runtime.Stable
import kotlinx.datetime.LocalDate

@Stable
internal data class ChannelScheduleForDay(
    val date: LocalDate,
    val segments: List<ChannelScheduleSegment>,
)
