package fr.outadoc.justchatting.utils.http

import fr.outadoc.justchatting.component.chatapi.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.utils.logging.logDebug
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.DarwinClientEngineConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

actual class HttpClientProvider(
    private val json: Json,
    private val preferenceRepository: PreferenceRepository,
    private val oAuthAppCredentials: OAuthAppCredentials,
) {
    actual fun getBaseClient(): HttpClient {
        return baseHttpClient()
    }

    actual fun getTwitchClient(): HttpClient {
        return baseHttpClient {
            defaultRequest {
                header("Client-ID", oAuthAppCredentials.clientId)
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val appUser = preferenceRepository
                            .currentPreferences.first()
                            .appUser

                        when (appUser) {
                            is AppUser.LoggedIn -> {
                                BearerTokens(
                                    accessToken = appUser.token,
                                    refreshToken = "",
                                )
                            }

                            is AppUser.NotValidated -> {
                                BearerTokens(
                                    accessToken = appUser.token,
                                    refreshToken = "",
                                )
                            }

                            is AppUser.NotLoggedIn -> null
                        }
                    }

                    refreshTokens {
                        preferenceRepository.updatePreferences { current ->
                            current.copy(
                                appUser = AppUser.NotLoggedIn,
                            )
                        }
                        null
                    }
                }
            }
        }
    }

    private fun baseHttpClient(
        block: HttpClientConfig<DarwinClientEngineConfig>.() -> Unit = {},
    ): HttpClient {
        return HttpClient(Darwin) {
            install(HttpCache)

            install(ContentNegotiation) {
                json(json)
            }

            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) = logDebug<HttpClient> { message }
                }
            }

            install(WebSockets) {
                extensions {
                    install(FrameLogger)
                }

                contentConverter = KotlinxWebsocketSerializationConverter(json)
            }

            expectSuccess = true

            block()
        }
    }
}
