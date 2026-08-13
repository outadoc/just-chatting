package fr.outadoc.justchatting.feature.timeline.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
public data class FullSchedule(
    val past: ImmutableList<DaySchedule> = persistentListOf(),
    val live: ImmutableList<UserStream> = persistentListOf(),
    val future: ImmutableList<DaySchedule> = persistentListOf(),
)
