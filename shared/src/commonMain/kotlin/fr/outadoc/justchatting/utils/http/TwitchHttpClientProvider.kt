package fr.outadoc.justchatting.utils.http

import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import kotlinx.coroutines.flow.first

internal class TwitchHttpClientProvider(
    private val baseHttpClientProvider: BaseHttpClientProvider,
    private val preferenceRepository: PreferenceRepository,
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
                        preferenceRepository.currentPreferences.first()
                            .apiToken?.let { token ->
                                BearerTokens(
                                    accessToken = token,
                                    refreshToken = "",
                                )
                            }
                    }

                    refreshTokens {
                        preferenceRepository.updatePreferences { prefs ->
                            prefs.copy(apiToken = null)
                        }
                        null
                    }
                }
            }
        }
    }
}
