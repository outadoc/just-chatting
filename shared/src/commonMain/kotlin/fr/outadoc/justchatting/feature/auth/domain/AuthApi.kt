package fr.outadoc.justchatting.feature.auth.domain

import fr.outadoc.justchatting.feature.auth.domain.model.AuthValidationResponse

internal interface AuthApi {
    suspend fun validateToken(token: String): Result<AuthValidationResponse>
    suspend fun revokeToken(clientId: String, token: String): Result<Unit>
}
