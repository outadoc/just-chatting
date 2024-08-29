package fr.outadoc.justchatting.feature.auth.domain.model

internal class AuthValidationResponse(
    val clientId: String,
    val login: String,
    val userId: String,
    val scopes: Set<String>,
)
