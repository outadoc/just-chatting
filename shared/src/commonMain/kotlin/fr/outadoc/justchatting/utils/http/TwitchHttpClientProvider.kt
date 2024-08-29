package fr.outadoc.justchatting.utils.http

import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import kotlinx.coroutines.flow.first

internal class TwitchHttpClientProvider(
    private val baseHttpClientProvider: BaseHttpClientProvider,
    private val authRepository: AuthRepository,
    private val oAuthAppCredentials: OAuthAppCredentials,
) {
    fun get(): HttpClient {
        return baseHttpClientProvider.get {
            defaultRequest {
                header("Client-ID", oAuthAppCredentials.clientId)
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        when (val appUser = authRepository.currentUser.first()) {
                            is AppUser.LoggedIn -> {
                                BearerTokens(
                                    accessToken = appUser.token,
                                    refreshToken = "",
                                )
                            }

                            is AppUser.NotLoggedIn -> null
                        }
                    }

                    refreshTokens {
                        authRepository.logout()
                        null
                    }
                }
            }
        }
    }
}
