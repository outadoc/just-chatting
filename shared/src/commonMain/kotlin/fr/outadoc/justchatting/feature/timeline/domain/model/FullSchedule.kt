package fr.outadoc.justchatting.feature.timeline.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.LocalDate

@Immutable
internal data class FullSchedule(
    val past: ImmutableMap<LocalDate, List<ChannelScheduleSegment>> = persistentMapOf(),
    val live: ImmutableList<UserStream> = persistentListOf(),
    val future: ImmutableMap<LocalDate, List<ChannelScheduleSegment>> = persistentMapOf(),
)
