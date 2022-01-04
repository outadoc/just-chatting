package com.github.andreyasadchy.xtra.repository

import com.github.andreyasadchy.xtra.api.IdApi
import com.github.andreyasadchy.xtra.model.id.ValidationResponse
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: IdApi) {

    suspend fun validate(token: String): ValidationResponse? = withContext(Dispatchers.IO) {
        api.validateToken(TwitchApiHelper.addTokenPrefix(token))
    }

    suspend fun revoke(clientId: String?, token: String) = withContext(Dispatchers.IO) {
        api.revokeToken(clientId!!, token)
    }
}
