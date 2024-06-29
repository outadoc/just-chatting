package fr.outadoc.justchatting.feature.auth.domain

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.auth.domain.model.AuthValidationResponse
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

internal class AuthRepository(
    private val api: AuthApi,
    private val preferencesRepository: PreferenceRepository,
    private val oAuthAppCredentials: OAuthAppCredentials,
) {
    suspend fun validate(token: String): Result<AuthValidationResponse> =
        withContext(DispatchersProvider.io) {
            api.validateToken(token)
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

    fun getExternalAuthorizeUrl(): Uri {
        return api.getExternalAuthorizeUrl(oAuthAppCredentials)
    }
}
