package fr.outadoc.justchatting.feature.auth.data

import fr.outadoc.justchatting.feature.auth.data.model.ValidationResponse

internal interface IdApi {
    suspend fun validateToken(token: String): Result<ValidationResponse>
    suspend fun revokeToken(clientId: String, token: String): Result<Unit>
}
