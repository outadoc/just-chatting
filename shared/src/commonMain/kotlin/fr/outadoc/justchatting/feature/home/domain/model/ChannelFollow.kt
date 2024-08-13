package fr.outadoc.justchatting.feature.home.domain.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class ChannelFollow(
    val user: User,
    val followedAt: String,
)
