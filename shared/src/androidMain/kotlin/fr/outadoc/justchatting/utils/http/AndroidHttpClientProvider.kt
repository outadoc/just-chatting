package fr.outadoc.justchatting.utils.http

import fr.outadoc.justchatting.utils.logging.logDebug
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
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

internal class AndroidHttpClientProvider(
    private val json: Json,
) : BaseHttpClientProvider {

    override fun get(block: HttpClientConfig<*>.() -> Unit): HttpClient {
        return HttpClient(CIO) {
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

            engine {
                requestTimeout = 30.seconds.inWholeMilliseconds

                endpoint {
                    connectTimeout = 10.seconds.inWholeMilliseconds
                }
            }

            block()
        }
    }
}
