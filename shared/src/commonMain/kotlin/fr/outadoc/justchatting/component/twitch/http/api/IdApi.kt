package fr.outadoc.justchatting.component.twitch.http.api

import fr.outadoc.justchatting.component.twitch.http.model.ValidationResponse

interface IdApi {
    suspend fun validateToken(token: String): ValidationResponse?
    suspend fun revokeToken(clientId: String, token: String)
}
