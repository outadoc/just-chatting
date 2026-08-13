package fr.outadoc.justchatting.feature.chat.data.irc

import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesRepository
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
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
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class LiveChatWebSocketTest {
    private class Harness(
        val server: FakeIrcServer,
        val socket: LiveChatWebSocket,
        val networkStateObserver: FakeNetworkStateObserver,
        val recentMessagesApi: FakeRecentMessagesApi,
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
        enableRecentMessages: Boolean = false,
        recentMessages: List<String> = emptyList(),
        initialNetworkState: NetworkStateObserver.NetworkState = NetworkStateObserver.NetworkState.Available,
        messageTimeout: Duration = 6.minutes,
        block: suspend Harness.() -> Unit,
    ) = runBlocking {
        val server = FakeIrcServer()
        val endpoint = server.start()
        val httpClient = HttpClient { install(WebSockets) }
        val networkStateObserver = FakeNetworkStateObserver(initialNetworkState)
        val recentMessagesApi = FakeRecentMessagesApi(recentMessages)
        val dispatchersProvider = RealDispatchersProvider()
        val parser = TwitchIrcCommandParser(testClock)

        val socket =
            LiveChatWebSocket(
                networkStateObserver = networkStateObserver,
                clock = testClock,
                parser = parser,
                httpClient = httpClient,
                recentMessagesRepository =
                    RecentMessagesRepository(
                        recentMessagesApi = recentMessagesApi,
                        parser = parser,
                        dispatchersProvider = dispatchersProvider,
                    ),
                preferencesRepository =
                    FakePreferenceRepository(
                        AppPreferences(enableRecentMessages = enableRecentMessages),
                    ),
                dispatchersProvider = dispatchersProvider,
                endpoint = endpoint,
                messageTimeout = messageTimeout,
            )

        val harness = Harness(server, socket, networkStateObserver, recentMessagesApi, this)
        try {
            harness.block()
        } finally {
            harness.collectJob?.cancelAndJoin()
            httpClient.close()
            server.stop()
        }
    }

    @Test
    fun `connects with anonymous handshake and emits join event`() =
        webSocketTest {
            startCollecting()

            val connection = server.awaitConnection()

            assertTrue(connection.awaitLine().startsWith("NICK justinfan"))
            assertEquals("CAP REQ :twitch.tv/tags twitch.tv/commands", connection.awaitLine())
            assertEquals("JOIN #$TEST_CHANNEL_LOGIN", connection.awaitLine())

            assertEquals(
                ChatEvent.Message.Join(
                    timestamp = testClock.now(),
                    channelLogin = TEST_CHANNEL_LOGIN,
                ),
                awaitEvent(),
            )
        }

    @Test
    fun `emits chat messages received from the server`() =
        webSocketTest {
            startCollecting()

            val connection = server.awaitConnection()
            awaitEvent() // Join

            connection.send(SAMPLE_PRIVMSG)

            val message = assertIs<ChatEvent.Message.ChatMessage>(awaitEvent())
            assertEquals("ronni", message.userLogin)
            assertEquals("Kappa Keepo Kappa", message.message)
        }

    @Test
    fun `replies to server pings`() =
        webSocketTest {
            startCollecting()

            val connection = server.awaitConnection()
            repeat(3) { connection.awaitLine() } // Handshake

            connection.send("PING :tmi.twitch.tv")

            assertEquals("PONG :tmi.twitch.tv", connection.awaitLine())
        }

    @Test
    fun `does not emit notice or userstate events`() =
        webSocketTest {
            startCollecting()

            val connection = server.awaitConnection()
            awaitEvent() // Join

            connection.send(SAMPLE_NOTICE)
            connection.send(SAMPLE_USERSTATE)
            connection.send(SAMPLE_PRIVMSG)

            // The notice and userstate should have been skipped
            assertIs<ChatEvent.Message.ChatMessage>(awaitEvent())
        }

    @Test
    fun `reconnects after the connection is lost`() =
        webSocketTest {
            startCollecting()

            val firstConnection = server.awaitConnection()
            awaitEvent() // Join
            firstConnection.close()

            val secondConnection = server.awaitConnection()
            assertTrue(secondConnection.awaitLine().startsWith("NICK justinfan"))

            assertEquals(
                ChatEvent.Message.Join(
                    timestamp = testClock.now(),
                    channelLogin = TEST_CHANNEL_LOGIN,
                ),
                awaitEvent(),
            )
        }

    @Test
    fun `tracks listeners and liveness in connection status`() =
        webSocketTest {
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

    @Test
    fun `waits for network availability before connecting`() =
        webSocketTest(
            initialNetworkState = NetworkStateObserver.NetworkState.Unavailable,
        ) {
            startCollecting()

            assertNull(server.awaitConnectionOrNull(timeout = 500.milliseconds))

            networkStateObserver.mutableState.value = NetworkStateObserver.NetworkState.Available

            server.awaitConnection()
        }

    @Test
    fun `replays recent messages after joining live chat`() =
        webSocketTest(
            enableRecentMessages = true,
            recentMessages = listOf(SAMPLE_PRIVMSG),
        ) {
            startCollecting()

            assertIs<ChatEvent.Message.Join>(awaitEvent())

            val recentMessage = assertIs<ChatEvent.Message.ChatMessage>(awaitEvent())
            assertEquals("Kappa Keepo Kappa", recentMessage.message)
        }

    @Test
    fun `does not replay recent messages when disabled`() =
        webSocketTest(
            enableRecentMessages = false,
            recentMessages = listOf(SAMPLE_PRIVMSG),
        ) {
            startCollecting()

            assertIs<ChatEvent.Message.Join>(awaitEvent())
        }

    @Test
    fun `reconnects when the server goes silent`() =
        webSocketTest(
            messageTimeout = 2.seconds,
        ) {
            startCollecting()

            server.awaitConnection()
            awaitEvent() // Join

            // Say nothing: the watchdog should drop the connection and reconnect
            val secondConnection = server.awaitConnection()
            assertTrue(secondConnection.awaitLine().startsWith("NICK justinfan"))
        }

    @Test
    fun `backfills messages missed while network was out`() =
        webSocketTest(
            enableRecentMessages = true,
            recentMessages = listOf(privMsg("old message", timestamp = 1_000_000_000_000)),
        ) {
            startCollecting()

            assertIs<ChatEvent.Message.Join>(awaitEvent())
            assertEquals("old message", assertIs<ChatEvent.Message.ChatMessage>(awaitEvent()).message)

            val connection = server.awaitConnection()
            connection.send(privMsg("live message", timestamp = 1_000_000_100_000))
            assertEquals("live message", assertIs<ChatEvent.Message.ChatMessage>(awaitEvent()).message)

            recentMessagesApi.messages =
                listOf(
                    privMsg("old message", timestamp = 1_000_000_000_000),
                    privMsg("missed message", timestamp = 1_000_000_200_000),
                )

            networkStateObserver.mutableState.value = NetworkStateObserver.NetworkState.Unavailable
            connection.awaitClose()
            networkStateObserver.mutableState.value = NetworkStateObserver.NetworkState.Available

            // Only the messages newer than the last one received should be replayed
            assertIs<ChatEvent.Message.Join>(awaitEvent())
            assertEquals(
                "missed message",
                assertIs<ChatEvent.Message.ChatMessage>(awaitEvent()).message,
            )
        }

    @Test
    fun `does not lose messages sent while recent messages are loading`() =
        webSocketTest(
            enableRecentMessages = true,
            recentMessages = listOf(privMsg("recent message", timestamp = 1_000_000_000_000)),
        ) {
            recentMessagesApi.responseDelay = 500.milliseconds

            startCollecting()

            val connection = server.awaitConnection()
            repeat(3) { connection.awaitLine() } // Handshake

            // The channel is joined at this point, but the recent messages are
            // still being fetched: this message should be buffered, not dropped
            connection.send(privMsg("live message", timestamp = 1_000_000_100_000))

            assertIs<ChatEvent.Message.Join>(awaitEvent())
            assertEquals(
                "recent message",
                assertIs<ChatEvent.Message.ChatMessage>(awaitEvent()).message,
            )
            assertEquals("live message", assertIs<ChatEvent.Message.ChatMessage>(awaitEvent()).message)
        }

    @Test
    fun `does not replay the same messages twice after reconnecting`() =
        webSocketTest(
            enableRecentMessages = true,
            recentMessages = listOf(privMsg("recent message", timestamp = 1_000_000_000_000)),
        ) {
            startCollecting()

            assertIs<ChatEvent.Message.Join>(awaitEvent())
            assertEquals(
                "recent message",
                assertIs<ChatEvent.Message.ChatMessage>(awaitEvent()).message,
            )

            val firstConnection = server.awaitConnection()
            firstConnection.close()

            val secondConnection = server.awaitConnection()
            assertIs<ChatEvent.Message.Join>(awaitEvent())

            // The recent message was already replayed; the next event should be the live one
            secondConnection.send(privMsg("live message", timestamp = 1_000_000_100_000))
            assertEquals("live message", assertIs<ChatEvent.Message.ChatMessage>(awaitEvent()).message)
        }
}
