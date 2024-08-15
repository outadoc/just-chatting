package fr.outadoc.justchatting.feature.home.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
internal data class ChannelFollow(
    val user: User,
    val followedAt: Instant,
)
