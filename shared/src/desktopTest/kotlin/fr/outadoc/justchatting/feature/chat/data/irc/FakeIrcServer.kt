package fr.outadoc.justchatting.feature.chat.data.irc

import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A local websocket server that stands in for the Twitch IRC endpoint in tests.
 */
internal class FakeIrcServer {
    class Connection(
        private val session: DefaultWebSocketSession,
    ) {
        val receivedLines = Channel<String>(Channel.UNLIMITED)
        val closed = CompletableDeferred<Unit>()

        suspend fun awaitLine(): String = withTimeout(10.seconds) { receivedLines.receive() }

        suspend fun awaitClose() = withTimeout(10.seconds) { closed.await() }

        suspend fun send(line: String) = session.send(line)

        suspend fun close() = session.close(CloseReason(CloseReason.Codes.NORMAL, "bye"))
    }

    private val connections = Channel<Connection>(Channel.UNLIMITED)
    private var server: EmbeddedServer<*, *>? = null

    suspend fun start(): String {
        val server =
            embeddedServer(CIO, port = 0) {
                install(WebSockets)
                routing {
                    webSocket("/") {
                        val connection = Connection(this)
                        connections.send(connection)
                        try {
                            for (frame in incoming) {
                                if (frame is Frame.Text) {
                                    frame
                                        .readText()
                                        .lines()
                                        .filter { it.isNotBlank() }
                                        .forEach { line -> connection.receivedLines.send(line) }
                                }
                            }
                        } finally {
                            connection.closed.complete(Unit)
                        }
                    }
                }
            }.start(wait = false)

        this.server = server

        val port =
            server.engine
                .resolvedConnectors()
                .first()
                .port
        return "ws://127.0.0.1:$port"
    }

    fun stop() {
        server?.stop(gracePeriodMillis = 0, timeoutMillis = 0)
    }

    suspend fun awaitConnection(): Connection = withTimeout(10.seconds) { connections.receive() }

    suspend fun awaitConnectionOrNull(timeout: Duration): Connection? = withTimeoutOrNull(timeout) { connections.receive() }
}
