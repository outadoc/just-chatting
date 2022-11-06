package fr.outadoc.justchatting.repository

import fr.outadoc.justchatting.api.IdApi
import fr.outadoc.justchatting.model.id.ValidationResponse
import fr.outadoc.justchatting.util.withBearerPrefix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AuthRepository(
    private val api: IdApi,
    private val preferencesRepository: PreferenceRepository
) {

    suspend fun validate(token: String): ValidationResponse? =
        withContext(Dispatchers.IO) {
            api.validateToken(token.withBearerPrefix())
        }

    suspend fun revokeToken() =
        withContext(Dispatchers.IO) {
            val prefs = preferencesRepository.currentPreferences.first()
            api.revokeToken(
                clientId = prefs.helixClientId,
                token = prefs.appUser.helixToken ?: return@withContext
            )
        }
}
