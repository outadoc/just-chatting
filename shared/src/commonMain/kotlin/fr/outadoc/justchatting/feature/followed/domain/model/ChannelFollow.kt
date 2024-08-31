package fr.outadoc.justchatting.feature.followed.domain.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.datetime.Instant

@Immutable
internal data class ChannelFollow(
    val user: User,
    val followedAt: Instant,
)