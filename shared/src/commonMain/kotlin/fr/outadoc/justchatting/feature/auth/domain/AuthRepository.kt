package fr.outadoc.justchatting.feature.auth.domain

import fr.outadoc.justchatting.feature.auth.data.IdApi
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.auth.domain.model.ValidationResponse
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

internal class AuthRepository(
    private val api: IdApi,
    private val preferencesRepository: PreferenceRepository,
    private val oAuthAppCredentials: OAuthAppCredentials,
) {
    suspend fun validate(token: String): Result<ValidationResponse> =
        withContext(DispatchersProvider.io) {
            api.validateToken(token)
                .map { response ->
                    ValidationResponse(
                        clientId = response.clientId,
                        login = response.login,
                        userId = response.userId,
                    )
                }
        }

    suspend fun revokeToken(): Result<Unit> =
        withContext(DispatchersProvider.io) {
            val user = preferencesRepository
                .currentPreferences.first()
                .appUser

            if (user !is AppUser.LoggedIn) {
                return@withContext Result.failure(IllegalStateException("User is not logged in"))
            }

            api.revokeToken(
                clientId = oAuthAppCredentials.clientId,
                token = user.token,
            )
        }
}
