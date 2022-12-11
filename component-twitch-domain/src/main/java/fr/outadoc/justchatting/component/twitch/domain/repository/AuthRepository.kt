package fr.outadoc.justchatting.component.twitch.domain.repository

import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.component.twitch.api.IdApi
import fr.outadoc.justchatting.component.twitch.model.OAuthAppCredentials
import fr.outadoc.justchatting.component.twitch.model.ValidationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AuthRepository(
    private val api: IdApi,
    private val preferencesRepository: PreferenceRepository,
    private val oAuthAppCredentials: OAuthAppCredentials
) {
    suspend fun validate(): ValidationResponse? =
        withContext(Dispatchers.IO) {
            api.validateToken()
        }

    suspend fun revokeToken() =
        withContext(Dispatchers.IO) {
            val prefs = preferencesRepository.currentPreferences.first()
            api.revokeToken(
                clientId = oAuthAppCredentials.clientId,
                token = prefs.appUser.helixToken ?: return@withContext
            )
        }
}
