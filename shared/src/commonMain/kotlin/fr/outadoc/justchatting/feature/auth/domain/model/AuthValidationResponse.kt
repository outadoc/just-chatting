package fr.outadoc.justchatting.feature.auth.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableSet

@Immutable
internal class AuthValidationResponse(
    val clientId: String,
    val login: String,
    val userId: String,
    val scopes: ImmutableSet<String>,
)
