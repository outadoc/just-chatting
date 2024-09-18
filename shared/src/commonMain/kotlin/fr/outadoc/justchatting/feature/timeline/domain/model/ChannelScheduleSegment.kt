package fr.outadoc.justchatting.feature.timeline.domain.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.datetime.Instant

@Immutable
internal data class ChannelScheduleSegment(
    val id: String,
    val user: User,
    val startTime: Instant,
    val endTime: Instant?,
    val title: String,
    val canceledUntil: Instant? = null,
    val category: StreamCategory? = null,
)
