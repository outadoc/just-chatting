package fr.outadoc.justchatting.feature.chat.data.irc

import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesRepository
import fr.outadoc.justchatting.feature.chat.domain.handler.ChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import fr.outadoc.justchatting.utils.core.delayWithJitter
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.logging.logInfo
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeout
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Live logged-out chat thread.
 *
 * Maintains a websocket connection to the IRC Twitch chat and notifies of all messages
 * and commands, except NOTICE and USERSTATE which are handled by [LoggedInChatWebSocket].
 */
internal class LiveChatWebSocket(
    private val networkStateObserver: NetworkStateObserver,
    private val clock: Clock,
    private val parser: TwitchIrcCommandParser,
    private val httpClient: HttpClient,
    private val recentMessagesRepository: RecentMessagesRepository,
    private val preferencesRepository: PreferenceRepository,
    private val dispatchersProvider: DispatchersProvider,
    private val endpoint: String = DEFAULT_ENDPOINT,
    private val messageTimeout: Duration = DEFAULT_MESSAGE_TIMEOUT,
) : ChatEventHandler {
    companion object {
        private const val DEFAULT_ENDPOINT = "wss://irc-ws.chat.twitch.tv"

        // Twitch pings us about every five minutes; if we go longer than this
        // without receiving anything, assume the connection is half-open.
        private val DEFAULT_MESSAGE_TIMEOUT = 6.minutes
    }

    private val _connectionStatus: MutableStateFlow<ConnectionStatus> =
        MutableStateFlow(
            ConnectionStatus(
                isAlive = false,
                registeredListeners = 0,
            ),
        )

    override val connectionStatus = _connectionStatus.asStateFlow()

    override fun getEventFlow(
        channelId: String,
        channelLogin: String,
        appUser: AppUser.LoggedIn,
    ): Flow<ChatEvent> = channelFlow {
        var lastMessageReceivedAt: Instant? = null
        var recentMessagesLoaded = false
        _connectionStatus.update { it.copy(registeredListeners = it.registeredListeners + 1) }
        try {
            networkStateObserver.state.collectLatest { netState ->
                if (netState is NetworkStateObserver.NetworkState.Available) {
                    logDebug<LiveChatWebSocket> { "Network is available, listening" }
                    if (!recentMessagesLoaded || lastMessageReceivedAt != null) {
                        recentMessagesLoaded = true
                        loadRecentMessages(channelLogin, lastMessageReceivedAt)
                    }
                    while (currentCoroutineContext().isActive) {
                        try {
                            listen(
                                channelLogin = channelLogin,
                                onMessageReceived = { timestamp ->
                                    lastMessageReceivedAt = timestamp
                                },
                            )
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            logError<LiveChatWebSocket>(e) { "Socket was closed" }
                        }
                        delayWithJitter(1.seconds, maxJitter = 3.seconds)
                    }
                } else {
                    logDebug<LiveChatWebSocket> { "Network is out, waiting" }
                    _connectionStatus.update { it.copy(isAlive = false) }
                }
            }
        } finally {
            _connectionStatus.update { it.copy(registeredListeners = it.registeredListeners - 1) }
        }
    }.flowOn(dispatchersProvider.io)

    private suspend fun ProducerScope<ChatEvent>.listen(
        channelLogin: String,
        onMessageReceived: (Instant) -> Unit,
    ) {
        httpClient.webSocket(endpoint) {
            logDebug<LiveChatWebSocket> { "Socket open, saying hello" }
            _connectionStatus.update { it.copy(isAlive = true) }
            try {
                // random number between 1000 and 9999
                send("NICK justinfan${Random.nextInt(1000, 10_000)}")
                send("CAP REQ :twitch.tv/tags twitch.tv/commands")
                send("JOIN #$channelLogin")

                this@listen.send(
                    ChatEvent.Message.Join(
                        timestamp = clock.now(),
                        channelLogin = channelLogin,
                    ),
                )

                // Receive messages
                while (isActive) {
                    val received =
                        try {
                            withTimeout(messageTimeout) { incoming.receive() }
                        } catch (e: TimeoutCancellationException) {
                            logError<LiveChatWebSocket>(e) { "No message received in $messageTimeout, closing socket" }
                            break
                        }

                    when (received) {
                        is Frame.Text -> {
                            received
                                .readText()
                                .lines()
                                .filter { it.isNotBlank() }
                                .forEach { line ->
                                    handleMessage(
                                        received = line,
                                        emit = { event -> this@listen.send(event) },
                                        onMessageReceived = onMessageReceived,
                                    )
                                }
                        }

                        else -> {}
                    }
                }
            } finally {
                _connectionStatus.update { it.copy(isAlive = false) }
            }
        }
    }

    private suspend fun DefaultWebSocketSession.handleMessage(
        received: String,
        emit: suspend (ChatEvent) -> Unit,
        onMessageReceived: (Instant) -> Unit,
    ) {
        logInfo<LiveChatWebSocket> { "received: $received" }

        when (val command: ChatEvent? = parser.parse(received)) {
            is ChatEvent.Command.UserState,
            is ChatEvent.Message.Notice,
            -> {
                // Handled by LoggedInChatWebSocket
            }

            is ChatEvent.Message -> {
                emit(command)
                onMessageReceived(command.timestamp)
            }

            is ChatEvent.Command.RoomStateDelta,
            is ChatEvent.Command.ClearChat,
            is ChatEvent.Command.ClearMessage,
            -> {
                emit(command)
            }

            is ChatEvent.Command.Ping -> {
                send("PONG :tmi.twitch.tv")
            }

            null -> {}
        }
    }

    private suspend fun ProducerScope<ChatEvent>.loadRecentMessages(
        channelLogin: String,
        lastMessageReceivedAt: Instant?,
    ) {
        val prefs = preferencesRepository.currentPreferences.first()
        if (!prefs.enableRecentMessages) return

        recentMessagesRepository
            .loadRecentMessages(
                channelLogin = channelLogin,
                limit = AppPreferences.Defaults.RecentChatLimit,
            ).map { messages ->
                messages
                    .filterIsInstance<ChatEvent.Message>()
                    .filter { event ->
                        event.timestamp >= (lastMessageReceivedAt ?: Instant.DISTANT_PAST)
                    }
            }.fold(
                onSuccess = { events ->
                    events.forEach { event -> send(event) }
                },
                onFailure = { e ->
                    logError<LiveChatWebSocket>(e) { "Failed to load recent messages for channel $channelLogin" }
                },
            )
    }
}
