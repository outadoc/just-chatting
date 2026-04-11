package fr.outadoc.justchatting.utils.http

import fr.outadoc.justchatting.feature.preferences.presentation.AppVersionNameProvider
import fr.outadoc.justchatting.utils.logging.logDebug
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

internal class DesktopHttpClientProvider(
    private val json: Json,
    private val appVersionNameProvider: AppVersionNameProvider,
) : BaseHttpClientProvider {
    override fun get(block: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(Java) {
        install(HttpCache)

        install(HttpTimeout) {
            requestTimeoutMillis = 30.seconds.inWholeMilliseconds
            connectTimeoutMillis = 10.seconds.inWholeMilliseconds
        }

        install(ContentNegotiation) {
            json(json)
        }

        install(UserAgent) {
            val appVersion = appVersionNameProvider.appVersionName.orEmpty()
            agent = "JustChatting-fr.outadoc.justchatting/$appVersion (JVM)"
        }

        install(Logging) {
            level = LogLevel.ALL
            logger =
                object : Logger {
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
