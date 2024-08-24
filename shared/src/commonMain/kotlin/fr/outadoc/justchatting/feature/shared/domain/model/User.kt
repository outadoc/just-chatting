package fr.outadoc.justchatting.feature.shared.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
internal data class User(
    val id: String,
    val login: String,
    val displayName: String,
    val description: String,
    val profileImageUrl: String,
    val createdAt: Instant,
    val usedAt: Instant?,
)
