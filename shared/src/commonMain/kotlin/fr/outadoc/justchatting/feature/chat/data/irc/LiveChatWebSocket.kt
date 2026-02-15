package fr.outadoc.justchatting.feature.chat.data.irc

import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesRepository
import fr.outadoc.justchatting.feature.chat.domain.handler.ChatCommandHandlerFactory
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
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Live logged-out chat thread.
 *
 * Maintains a websocket connection to the IRC Twitch chat and notifies of all messages
 * and commands, except NOTICE and USERSTATE which are handled by [LoggedInChatWebSocket].
 */
internal class LiveChatWebSocket private constructor(
    private val networkStateObserver: NetworkStateObserver,
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

    override val eventFlow: Flow<ChatEvent> = channelFlow {
        _connectionStatus.update { it.copy(registeredListeners = 1) }
        try {
            networkStateObserver.state.collectLatest { netState ->
                if (netState is NetworkStateObserver.NetworkState.Available) {
                    logDebug<LiveChatWebSocket> { "Network is available, listening" }
                    loadRecentMessages()
                    while (currentCoroutineContext().isActive) {
                        _connectionStatus.update { it.copy(isAlive = true) }
                        try {
                            listen()
                        } catch (e: Exception) {
                            logError<LiveChatWebSocket>(e) { "Socket was closed" }
                        }
                        _connectionStatus.update { it.copy(isAlive = false) }
                        delayWithJitter(1.seconds, maxJitter = 3.seconds)
                    }
                } else {
                    logDebug<LiveChatWebSocket> { "Network is out, waiting" }
                    _connectionStatus.update { it.copy(isAlive = false) }
                }
            }
        } finally {
            _connectionStatus.update { it.copy(registeredListeners = 0) }
        }
    }.flowOn(DispatchersProvider.io)

    private suspend fun ProducerScope<ChatEvent>.listen() {
        httpClient.webSocket(ENDPOINT) {
            logDebug<LiveChatWebSocket> { "Socket open, saying hello" }

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
                when (val received = incoming.receive()) {
                    is Frame.Text -> {
                        received
                            .readText()
                            .lines()
                            .filter { it.isNotBlank() }
                            .forEach { line ->
                                handleMessage(line) { event ->
                                    this@listen.send(event)
                                }
                            }
                    }

                    else -> {}
                }
            }
        }
    }

    private suspend fun DefaultWebSocketSession.handleMessage(
        received: String,
        emit: suspend (ChatEvent) -> Unit,
    ) {
        logInfo<LiveChatWebSocket> { "received: $received" }

        when (val command: ChatEvent? = parser.parse(received)) {
            is ChatEvent.Command.UserState,
            is ChatEvent.Message.Notice,
            -> {
                // Handled by LoggedInChatWebSocket
            }

            is ChatEvent.Message -> {
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
                send("PONG :tmi.twitch.tv")
            }

            null -> {}
        }
    }

    private suspend fun ProducerScope<ChatEvent>.loadRecentMessages() {
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

    class Factory(
        private val clock: Clock,
        private val networkStateObserver: NetworkStateObserver,
        private val parser: TwitchIrcCommandParser,
        private val recentMessagesRepository: RecentMessagesRepository,
        private val preferencesRepository: PreferenceRepository,
        private val httpClient: HttpClient,
    ) : ChatCommandHandlerFactory {
        override fun create(
            channelLogin: String,
            channelId: String,
            appUser: AppUser.LoggedIn,
        ): LiveChatWebSocket = LiveChatWebSocket(
            networkStateObserver = networkStateObserver,
            clock = clock,
            parser = parser,
            httpClient = httpClient,
            recentMessagesRepository = recentMessagesRepository,
            preferencesRepository = preferencesRepository,
            channelLogin = channelLogin,
        )
    }
}
