package fr.outadoc.justchatting.feature.preferences.domain

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.auth.domain.AuthApi
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

internal class AuthRepository(
    private val preferenceRepository: PreferenceRepository,
    private val authApi: AuthApi,
    private val preferencesRepository: PreferenceRepository,
    private val oAuthAppCredentials: OAuthAppCredentials,
) {
    private val scope = CoroutineScope(SupervisorJob())

    val currentUser: StateFlow<AppUser> =
        preferenceRepository
            .currentPreferences
            .map { prefs -> prefs.apiToken }
            .distinctUntilChanged()
            .map { token ->
                when (token) {
                    null -> AppUser.NotLoggedIn
                    else -> {
                        authApi.validateToken(token)
                            .mapCatching { response ->
                                if (response.clientId != oAuthAppCredentials.clientId) {
                                    throw InvalidClientIdException()
                                }

                                // TODO validate scopes

                                response
                            }
                            .fold(
                                onSuccess = { response ->
                                    AppUser.LoggedIn(
                                        userId = response.userId,
                                        userLogin = response.login,
                                        token = token,
                                    )
                                },
                                onFailure = { exception ->
                                    logError<AuthRepository>(exception) { "Failed to validate token" }
                                    AppUser.NotLoggedIn
                                },
                            )
                    }
                }
            }
            .distinctUntilChanged()
            .stateIn(
                scope,
                initialValue = AppUser.NotLoggedIn,
                started = SharingStarted.Lazily,
            )

    suspend fun saveToken(token: String) {
        preferenceRepository.updatePreferences { prefs ->
            prefs.copy(apiToken = token)
        }
    }

    suspend fun logout() = withContext(DispatchersProvider.io) {
        val token = preferencesRepository
            .currentPreferences.first()
            .apiToken

        if (token == null) {
            logError<AuthRepository> { "User is already logged out" }
            return@withContext
        }

        authApi.revokeToken(
            clientId = oAuthAppCredentials.clientId,
            token = token,
        )
            .onFailure { exception ->
                logError<AuthRepository>(exception) { "Failed to revoke token" }
            }

        preferenceRepository.updatePreferences { prefs ->
            prefs.copy(
                apiToken = null,
            )
        }
    }

    fun getExternalAuthorizeUrl(): Uri {
        return authApi.getExternalAuthorizeUrl(
            oAuthAppCredentials = oAuthAppCredentials,
            scopes = SCOPES,
        )
    }

    private class InvalidClientIdException : Exception()

    private companion object {
        val SCOPES = setOf(
            "chat:read",
            "chat:edit",
            "user:read:follows",
        )
    }
}
