package com.github.andreyasadchy.xtra.repository

import com.github.andreyasadchy.xtra.api.IdApi
import com.github.andreyasadchy.xtra.model.id.ValidationResponse
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.TwitchApiHelper.addTokenPrefixHelix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: IdApi,
    private val authPreferencesRepository: AuthPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend fun validate(token: String): ValidationResponse? =
        withContext(Dispatchers.IO) {
            api.validateToken(token.addTokenPrefixHelix()).also {
                TwitchApiHelper.checkedValidation = true
            }
        }

    suspend fun revokeToken() =
        withContext(Dispatchers.IO) {
            val clientId = authPreferencesRepository.helixClientId.first() ?: return@withContext
            val token = userPreferencesRepository.user.first().helixToken ?: return@withContext
            api.revokeToken(clientId, token)
        }
}
