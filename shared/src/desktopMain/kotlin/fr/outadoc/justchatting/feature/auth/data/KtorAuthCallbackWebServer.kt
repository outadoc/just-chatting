package fr.outadoc.justchatting.feature.auth.data

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.utils.http.toUri
import fr.outadoc.justchatting.utils.logging.logDebug
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.MutableSharedFlow

internal class KtorAuthCallbackWebServer(
    private val oAuthAppCredentials: OAuthAppCredentials,
) : AuthCallbackWebServer {

    override val receivedUris = MutableSharedFlow<String>()

    private val server =
        embeddedServer(CIO, 45563) {
            install(CORS) {
                allowHost("just-chatting.app")
            }

            routing {
                get("/auth/callback") {
                    call.respond(HttpStatusCode.NoContent)

                    val asFragmentEncoded: Uri =
                        oAuthAppCredentials.redirectUri.toUri()
                            .buildUpon()
                            .encodedFragment(
                                call.request.uri.toUri().encodedQuery,
                            )
                            .build()

                    logDebug<KtorAuthCallbackWebServer> { "received: $asFragmentEncoded" }

                    receivedUris.emit(asFragmentEncoded.toString())
                }
            }
        }

    override fun start() {
        server.start(wait = false)
    }

    override fun stop() {
        server.stop()
    }
}
