package fr.outadoc.justchatting.feature.home.domain.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class UserStream(
    val user: User,
    val stream: Stream
)
