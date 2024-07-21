package fr.outadoc.justchatting.feature.home.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
internal data class ChannelScheduleSegment(
    val id: String,
    val startTime: Instant,
    val endTime: Instant,
    val title: String,
    val canceledUntil: Instant? = null,
    val category: StreamCategory? = null,
    val isRecurring: Boolean,
)
