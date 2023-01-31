package fr.outadoc.justchatting.component.chatapi.domain.repository

import fr.outadoc.justchatting.component.chatapi.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.component.chatapi.domain.model.ValidationResponse
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.component.twitch.http.api.IdApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AuthRepository(
    private val api: IdApi,
    private val preferencesRepository: PreferenceRepository,
    private val oAuthAppCredentials: OAuthAppCredentials,
) {
    suspend fun validate(): ValidationResponse? =
        withContext(Dispatchers.IO) {
            api.validateToken()?.let { response ->
                ValidationResponse(
                    clientId = response.clientId,
                    login = response.login,
                    userId = response.userId,
                )
            }
        }

    suspend fun revokeToken() {
        withContext(Dispatchers.IO) {
            val prefs = preferencesRepository.currentPreferences.first()
            api.revokeToken(
                clientId = oAuthAppCredentials.clientId,
                token = prefs.appUser.helixToken ?: return@withContext,
            )
        }
    }
}
