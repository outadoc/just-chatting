package fr.outadoc.justchatting.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import fr.outadoc.justchatting.component.chatapi.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.component.chatapi.domain.repository.AuthRepository
import fr.outadoc.justchatting.component.deeplink.DeeplinkParser
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.logging.FrameLogger
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import fr.outadoc.justchatting.utils.logging.logDebug
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.CIOEngineConfig
import io.ktor.client.engine.cio.endpoint
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
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import kotlin.time.Duration.Companion.minutes

val mainModule = module {
    single<Clock> { Clock.System }
    single<ConnectivityManager> { get<Context>().getSystemService()!! }
    single { NetworkStateObserver(get()) }
    single { AuthRepository(get(), get(), get()) }
    single { DeeplinkParser(get()) }

    single { baseHttpClient() }

    single(qualifier = named("twitch")) {
        baseHttpClient {
            defaultRequest {
                header("Client-ID", get<OAuthAppCredentials>().clientId)
            }

            install(Auth) {
                bearer {
                    val getToken: suspend () -> BearerTokens? = {
                        val preferenceRepository = get<PreferenceRepository>()
                        val user: AppUser = preferenceRepository.currentPreferences.first().appUser
                        user.helixToken?.let { token ->
                            BearerTokens(accessToken = token, refreshToken = "")
                        }
                    }

                    loadTokens { getToken() }
                    refreshTokens { getToken() }
                }
            }
        }
    }
}

private fun Scope.baseHttpClient(
    block: HttpClientConfig<CIOEngineConfig>.() -> Unit = {},
): HttpClient {
    return HttpClient(CIO) {
        install(HttpCache)

        install(ContentNegotiation) {
            json(get())
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

            contentConverter = KotlinxWebsocketSerializationConverter(get<Json>())
        }

        expectSuccess = true

        engine {
            requestTimeout = 1.minutes.inWholeMilliseconds
            endpoint {
                connectTimeout = 1.minutes.inWholeMilliseconds
            }
        }

        block()
    }
}
