package fr.outadoc.justchatting.feature.auth.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableSet

@Immutable
public class AuthValidationResponse(
    public val clientId: String,
    public val login: String,
    public val userId: String,
    public val scopes: ImmutableSet<String>,
)
