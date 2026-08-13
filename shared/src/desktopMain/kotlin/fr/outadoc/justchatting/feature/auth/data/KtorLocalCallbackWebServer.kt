package fr.outadoc.justchatting.feature.auth.data

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.deeplink.DeeplinkDefinitions
import fr.outadoc.justchatting.feature.shared.domain.TwitchApi
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

/**
 * Local-only HTTP server used on desktop to receive the OAuth redirect from the system browser.
 *
 * Since desktop has no OS-level deeplink handling, each supported deeplink also gets a mirrored
 * HTTP route here (matching its host/path from [DeeplinkDefinitions]), so it can be triggered for
 * local testing without needing a real deeplink, e.g.:
 *
 * ```
 * curl "http://localhost:45563/user/<login>"
 * ```
 */
internal class KtorLocalCallbackWebServer(
    private val oAuthAppCredentials: OAuthAppCredentials,
    private val twitchApi: TwitchApi,
) : LocalCallbackWebServer {
    override val receivedUris = MutableSharedFlow<String>()

    private var isStarted = false

    private val server =
        embeddedServer(CIO, 45563) {
            install(CORS) {
                allowHost("just-chatting.app")
            }

            routing {
                get("/auth/callback") {
                    call.respond(HttpStatusCode.NoContent)

                    val asFragmentEncoded: Uri =
                        oAuthAppCredentials.redirectUri
                            .toUri()
                            .buildUpon()
                            .encodedFragment(
                                call.request.uri
                                    .toUri()
                                    .encodedQuery,
                            ).build()

                    logDebug<KtorLocalCallbackWebServer> { "received auth callback: $asFragmentEncoded" }

                    receivedUris.emit(asFragmentEncoded.toString())
                }

                get("/user/{login}") {
                    val login = call.parameters["login"]
                    if (login == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }

                    // The deeplink itself carries a numeric user id, not a login, to match what
                    // the rest of the app expects (see User.id). Resolve it here so this route
                    // can be triggered with the more convenient login name.
                    val userId = twitchApi.getUsersByLogin(listOf(login)).firstOrNull()?.id
                    if (userId == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }

                    call.respond(HttpStatusCode.NoContent)

                    val uri =
                        DeeplinkDefinitions.ViewChannel
                            .buildUpon()
                            .appendPath(userId)
                            .build()

                    logDebug<KtorLocalCallbackWebServer> { "received deeplink: $uri" }

                    receivedUris.emit(uri.toString())
                }
            }
        }

    override fun start() {
        if (isStarted) return
        isStarted = true
        server.start(wait = false)
    }

    override fun stop() {
        if (!isStarted) return
        isStarted = false
        server.stop()
    }
}
