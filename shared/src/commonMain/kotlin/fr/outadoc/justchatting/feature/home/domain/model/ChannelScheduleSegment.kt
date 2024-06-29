package fr.outadoc.justchatting.feature.home.domain.model

import fr.outadoc.justchatting.component.twitch.http.model.StreamCategory
import kotlinx.datetime.Instant

internal data class ChannelScheduleSegment(
    val id: String,
    val startTime: Instant,
    val endTime: Instant,
    val title: String,
    val canceledUntil: Instant? = null,
    val category: StreamCategory? = null,
    val isRecurring: Boolean,
)
