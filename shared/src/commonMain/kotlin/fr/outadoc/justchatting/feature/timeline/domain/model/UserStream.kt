package fr.outadoc.justchatting.feature.timeline.domain.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.feature.shared.domain.model.User

@Immutable
public data class UserStream(
    val user: User,
    val stream: Stream,
)
