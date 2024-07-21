package fr.outadoc.justchatting.feature.home.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

@Immutable
internal data class ChannelScheduleForDay(
    val date: LocalDate,
    val segments: ImmutableList<ChannelScheduleSegment>,
)
