package fr.outadoc.justchatting.component.twitch.api

import fr.outadoc.justchatting.component.twitch.model.ValidationResponse

interface IdApi {

    suspend fun validateToken(): ValidationResponse?

    suspend fun revokeToken(clientId: String, token: String)
}
