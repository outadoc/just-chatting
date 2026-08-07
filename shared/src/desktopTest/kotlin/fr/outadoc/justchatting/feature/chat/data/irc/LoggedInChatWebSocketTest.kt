package fr.outadoc.justchatting.feature.chat.data.irc

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class LoggedInChatWebSocketTest {
    private class Harness(
        val server: FakeIrcServer,
        val socket: LoggedInChatWebSocket,
        private val scope: CoroutineScope,
    ) {
        val events = Channel<ChatEvent>(Channel.UNLIMITED)
        var collectJob: Job? = null

        fun startCollecting() {
            collectJob =
                scope.launch {
                    socket
                        .getEventFlow(
                            channelId = TEST_CHANNEL_ID,
                            channelLogin = TEST_CHANNEL_LOGIN,
                            appUser = testAppUser,
                        ).collect { event -> events.send(event) }
                }
        }

        suspend fun awaitEvent(): ChatEvent = withTimeout(10.seconds) { events.receive() }
    }

    private fun webSocketTest(
        messageTimeout: Duration = 6.minutes,
        block: suspend Harness.() -> Unit,
    ) = runBlocking {
        val server = FakeIrcServer()
        val endpoint = server.start()
        val httpClient = HttpClient { install(WebSockets) }

        val socket =
            LoggedInChatWebSocket(
                networkStateObserver = FakeNetworkStateObserver(),
                parser = TwitchIrcCommandParser(testClock),
                httpClient = httpClient,
                dispatchersProvider = RealDispatchersProvider(),
                endpoint = endpoint,
                messageTimeout = messageTimeout,
            )

        val harness = Harness(server, socket, this)
        try {
            harness.block()
        } finally {
            harness.collectJob?.cancelAndJoin()
            httpClient.close()
            server.stop()
        }
    }

    @Test
    fun `authenticates with the user token on connect`() = webSocketTest {
        startCollecting()

        val connection = server.awaitConnection()

        assertEquals("PASS oauth:${testAppUser.token}", connection.awaitLine())
        assertEquals("NICK ${testAppUser.userLogin}", connection.awaitLine())
        assertEquals("CAP REQ :twitch.tv/tags twitch.tv/commands", connection.awaitLine())
        assertEquals("JOIN #$TEST_CHANNEL_LOGIN", connection.awaitLine())
    }

    @Test
    fun `only emits notice and userstate events`() = webSocketTest {
        startCollecting()

        val connection = server.awaitConnection()

        connection.send(SAMPLE_PRIVMSG)
        connection.send(SAMPLE_NOTICE)
        connection.send(SAMPLE_USERSTATE)

        // The chat message should have been skipped
        assertIs<ChatEvent.Message.Notice>(awaitEvent())

        val userState = assertIs<ChatEvent.Command.UserState>(awaitEvent())
        assertEquals(listOf("0", "33", "50"), userState.emoteSets)
    }

    @Test
    fun `replies to server pings`() = webSocketTest {
        startCollecting()

        val connection = server.awaitConnection()
        repeat(4) { connection.awaitLine() } // Handshake

        connection.send("PING :tmi.twitch.tv")

        assertEquals("PONG :tmi.twitch.tv", connection.awaitLine())
    }

    @Test
    fun `reconnects after the connection is lost`() = webSocketTest {
        startCollecting()

        val firstConnection = server.awaitConnection()
        assertTrue(firstConnection.awaitLine().startsWith("PASS "))
        firstConnection.close()

        val secondConnection = server.awaitConnection()
        assertTrue(secondConnection.awaitLine().startsWith("PASS "))
    }

    @Test
    fun `reconnects when the server goes silent`() = webSocketTest(
        messageTimeout = 2.seconds,
    ) {
        startCollecting()

        server.awaitConnection()

        // Say nothing: the watchdog should drop the connection and reconnect
        val secondConnection = server.awaitConnection()
        assertTrue(secondConnection.awaitLine().startsWith("PASS "))
    }

    @Test
    fun `tracks listeners and liveness in connection status`() = webSocketTest {
        assertEquals(0, socket.connectionStatus.value.registeredListeners)

        startCollecting()
        server.awaitConnection()

        withTimeout(10.seconds) {
            socket.connectionStatus.first { status ->
                status.isAlive && status.registeredListeners == 1
            }
        }

        collectJob?.cancelAndJoin()

        withTimeout(10.seconds) {
            socket.connectionStatus.first { status ->
                !status.isAlive && status.registeredListeners == 0
            }
        }
    }
}
