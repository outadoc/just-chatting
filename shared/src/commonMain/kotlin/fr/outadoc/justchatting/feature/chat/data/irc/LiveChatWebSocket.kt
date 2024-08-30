package fr.outadoc.justchatting.feature.chat.data.irc

import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesRepository
import fr.outadoc.justchatting.feature.chat.domain.handler.ChatCommandHandlerFactory
import fr.outadoc.justchatting.feature.chat.domain.handler.ChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

/**
 * Live logged-out chat thread.
 *
 * Maintains a websocket connection to the IRC Twitch chat and notifies of all messages
 * and commands, except NOTICE and USERSTATE which are handled by [LoggedInChatWebSocket].
 */
internal class LiveChatWebSocket private constructor(
    networkStateObserver: NetworkStateObserver,
    private val scope: CoroutineScope,
    private val clock: Clock,
    private val parser: TwitchIrcCommandParser,
    private val httpClient: HttpClient,
    private val recentMessagesRepository: RecentMessagesRepository,
    private val preferencesRepository: PreferenceRepository,
    private val channelLogin: String,
) : ChatEventHandler {

    companion object {
        private const val ENDPOINT = "wss://irc-ws.chat.twitch.tv"
    }

    private val _connectionStatus: MutableStateFlow<ConnectionStatus> =
        MutableStateFlow(
            ConnectionStatus(
                isAlive = false,
                registeredListeners = 0,
            ),
        )

    override val connectionStatus = _connectionStatus.asStateFlow()

    private var lastMessageReceivedAt: Instant? = null
    private var isNetworkAvailable: Boolean = false

    init {
        scope.launch {
            networkStateObserver.state.collectLatest { state ->
                isNetworkAvailable = state is NetworkStateObserver.NetworkState.Available
            }
        }
    }

    override val eventFlow: Flow<ChatEvent> =
        flow {
            logDebug<LiveChatWebSocket> { "Starting job" }

            _connectionStatus.update { status -> status.copy(registeredListeners = 1) }

            while (true) {
                if (isNetworkAvailable) {
                    logDebug<LiveChatWebSocket> { "Network is available, listening" }
                    _connectionStatus.update { status -> status.copy(isAlive = true) }

                    loadRecentMessages()

                    try {
                        listen()
                    } catch (e: Exception) {
                        logError<LiveChatWebSocket>(e) { "Socket was closed" }
                    }
                } else {
                    logDebug<LiveChatWebSocket> { "Network is out, delay and retry" }
                    _connectionStatus.update { status -> status.copy(isAlive = false) }
                }

                delayWithJitter(1.seconds, maxJitter = 3.seconds)
            }
        }.onCompletion {
            _connectionStatus.update { status -> status.copy(registeredListeners = 0) }
        }

    private suspend fun FlowCollector<ChatEvent>.listen() {
        httpClient.webSocket(ENDPOINT) {
            logDebug<LiveChatWebSocket> { "Socket open, saying hello" }

            // random number between 1000 and 9999
            send("NICK justinfan${Random.nextInt(1000, 10_000)}")
            send("CAP REQ :twitch.tv/tags twitch.tv/commands")
            send("JOIN #$channelLogin")

            emit(
                ChatEvent.Message.Join(
                    timestamp = clock.now(),
                    channelLogin = channelLogin,
                ),
            )

            // Receive messages
            while (isActive) {
                when (val received = incoming.receive()) {
                    is Frame.Text -> {
                        received.readText()
                            .lines()
                            .filter { it.isNotBlank() }
                            .forEach { line ->
                                handleMessage(
                                    session = this,
                                    received = line
                                )
                            }
                    }

                    else -> {}
                }
            }
        }
    }

    private suspend fun FlowCollector<ChatEvent>.handleMessage(
        session: DefaultWebSocketSession,
        received: String
    ) {
        logInfo<LiveChatWebSocket> { "received: $received" }

        when (val command: ChatEvent? = parser.parse(received)) {
            is ChatEvent.Command.UserState,
            is ChatEvent.Message.Notice,
            -> {
                // Handled by LoggedInChatWebSocket
            }

            is ChatEvent.Message -> {
                // Remember time of last message so that we can restore lost messages after a connection loss
                lastMessageReceivedAt = command.timestamp

                emit(command)
            }

            is ChatEvent.Command.RoomStateDelta,
            is ChatEvent.Command.ClearChat,
            is ChatEvent.Command.ClearMessage,
            -> {
                emit(command)
            }

            is ChatEvent.Command.Ping -> {
                session.send("PONG :tmi.twitch.tv")
            }

            null -> {}
        }
    }

    private suspend fun FlowCollector<ChatEvent>.loadRecentMessages() {
        val prefs = preferencesRepository.currentPreferences.first()
        if (!prefs.enableRecentMessages) return

        recentMessagesRepository
            .loadRecentMessages(
                channelLogin = channelLogin,
                limit = AppPreferences.Defaults.RecentChatLimit,
            )
            .map { messages ->
                messages
                    .asFlow()
                    .filterIsInstance<ChatEvent.Message>()
                    .dropWhile { event ->
                        // Drop messages that were received before the last message we received
                        event.timestamp < (lastMessageReceivedAt ?: Instant.DISTANT_PAST)
                    }
            }
            .fold(
                onSuccess = { events ->
                    emitAll(events)
                },
                onFailure = { e ->
                    logError<LiveChatWebSocket>(e) { "Failed to load recent messages for channel $channelLogin" }
                },
            )
    }

    class Factory(
        private val clock: Clock,
        private val networkStateObserver: NetworkStateObserver,
        private val parser: TwitchIrcCommandParser,
        private val recentMessagesRepository: RecentMessagesRepository,
        private val preferencesRepository: PreferenceRepository,
        private val httpClient: HttpClient,
    ) : ChatCommandHandlerFactory {

        override fun create(
            scope: CoroutineScope,
            channelLogin: String,
            channelId: String,
        ): LiveChatWebSocket = LiveChatWebSocket(
            networkStateObserver = networkStateObserver,
            scope = scope,
            clock = clock,
            parser = parser,
            httpClient = httpClient,
            recentMessagesRepository = recentMessagesRepository,
            preferencesRepository = preferencesRepository,
            channelLogin = channelLogin,
        )
    }
}
