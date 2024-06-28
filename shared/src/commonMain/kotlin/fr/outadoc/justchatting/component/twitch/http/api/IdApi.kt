package fr.outadoc.justchatting.component.twitch.http.api

import fr.outadoc.justchatting.component.twitch.http.model.ValidationResponse

internal interface IdApi {
    suspend fun validateToken(token: String): Result<ValidationResponse>
    suspend fun revokeToken(clientId: String, token: String): Result<Unit>
}
