package fr.outadoc.justchatting.component.twitch.websocket.irc

import android.content.Context
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.ConnectionStatus
import fr.outadoc.justchatting.component.chatapi.common.handler.ChatCommandHandlerFactory
import fr.outadoc.justchatting.component.chatapi.common.handler.ChatEventHandler
import fr.outadoc.justchatting.component.preferences.data.AppPreferences
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.component.twitch.R
import fr.outadoc.justchatting.component.twitch.websocket.Defaults
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.IrcEvent
import fr.outadoc.justchatting.component.twitch.websocket.irc.recent.RecentMessagesRepository
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

/**
 * Live logged-out chat thread.
 *
 * Maintains a websocket connection to the IRC Twitch chat and notifies of all messages
 * and commands, except NOTICE and USERSTATE which are handled by [LoggedInChatWebSocket].
 */
class LiveChatWebSocket private constructor(
    networkStateObserver: NetworkStateObserver,
    private val scope: CoroutineScope,
    private val context: Context,
    private val clock: Clock,
    private val parser: TwitchIrcCommandParser,
    private val mapper: IrcMessageMapper,
    private val httpClient: HttpClient,
    private val recentMessagesRepository: RecentMessagesRepository,
    private val preferencesRepository: PreferenceRepository,
    private val channelLogin: String,
) : ChatEventHandler {

    companion object {
        private const val ENDPOINT = "wss://irc-ws.chat.twitch.tv"
    }

    private val _flow = MutableSharedFlow<ChatEvent>(
        replay = Defaults.EventBufferSize,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val commandFlow: Flow<ChatEvent> = _flow

    private val _connectionStatus: MutableStateFlow<ConnectionStatus> =
        MutableStateFlow(
            ConnectionStatus(
                isAlive = false,
                preventSendingMessages = false,
                registeredListeners = 0,
            ),
        )

    override val connectionStatus = _connectionStatus.asStateFlow()

    private var isNetworkAvailable: Boolean = false
    private var socketJob: Job? = null

    init {
        scope.launch {
            networkStateObserver.state.collectLatest { state ->
                isNetworkAvailable = state is NetworkStateObserver.NetworkState.Available
            }
        }
    }

    override fun start() {
        socketJob = scope.launch(Dispatchers.IO + SupervisorJob()) {
            logDebug<LiveChatWebSocket> { "Starting job" }

            _connectionStatus.update { status -> status.copy(registeredListeners = 1) }

            loadRecentMessages()

            while (isActive) {
                if (isNetworkAvailable) {
                    logDebug<LiveChatWebSocket> { "Network is available, listening" }
                    _connectionStatus.update { status -> status.copy(isAlive = true) }

                    try {
                        listen()
                    } catch (e: Exception) {
                        logError<LiveChatWebSocket>(e) { "Socket was closed" }
                    }
                } else {
                    logDebug<LiveChatWebSocket> { "Network is out, delay and retry" }
                    _connectionStatus.update { status -> status.copy(isAlive = false) }
                }

                if (isActive) {
                    delayWithJitter(1.seconds, maxJitter = 3.seconds)
                }
            }
        }
    }

    private suspend fun listen() {
        httpClient.webSocket(ENDPOINT) {
            logDebug<LiveChatWebSocket> { "Socket open, saying hello" }

            // random number between 1000 and 9999
            send("NICK justinfan${Random.nextInt(1000, 10_000)}")
            send("CAP REQ :twitch.tv/tags twitch.tv/commands")
            send("JOIN #$channelLogin")

            _flow.emit(
                ChatEvent.Message.Highlighted(
                    timestamp = clock.now(),
                    title = context.getString(R.string.chat_join, channelLogin),
                    subtitle = null,
                    body = null,
                ),
            )

            // Receive messages
            while (isActive) {
                when (val received = incoming.receive()) {
                    is Frame.Text -> {
                        received.readText()
                            .lines()
                            .filter { it.isNotBlank() }
                            .forEach { line -> handleMessage(line) }
                    }

                    else -> {}
                }
            }
        }
    }

    private suspend fun DefaultWebSocketSession.handleMessage(received: String) {
        logInfo<LiveChatWebSocket> { "received: $received" }

        when (val command: IrcEvent? = parser.parse(received)) {
            is IrcEvent.Command.UserState, is IrcEvent.Message.Notice -> {
                // Handled by LoggedInChatWebSocket
            }

            is IrcEvent.Message -> {
                _flow.emit(mapper.mapMessage(command))
            }

            is IrcEvent.Command.RoomStateDelta -> {
                _flow.emit(
                    ChatEvent.RoomStateDelta(
                        isEmoteOnly = command.isEmoteOnly,
                        minFollowDuration = command.minFollowDuration,
                        uniqueMessagesOnly = command.uniqueMessagesOnly,
                        slowModeDuration = command.slowModeDuration,
                        isSubOnly = command.isSubOnly,
                    ),
                )
            }

            is IrcEvent.Command.ClearChat -> {
                _flow.emit(
                    ChatEvent.RemoveContent(
                        upUntil = command.timestamp,
                        matchingUserId = command.targetUserId,
                    ),
                )

                mapper.mapOptional(command)?.let { message ->
                    _flow.emit(message)
                }
            }

            is IrcEvent.Command.ClearMessage -> {
                _flow.emit(
                    ChatEvent.RemoveContent(
                        upUntil = command.timestamp,
                        matchingMessageId = command.targetMessageId,
                    ),
                )

                mapper.mapOptional(command)?.let { message ->
                    _flow.emit(message)
                }
            }

            is IrcEvent.Command.Ping -> {
                send("PONG :tmi.twitch.tv")
            }

            null -> {}
        }
    }

    override fun disconnect() {
        scope.launch {
            _connectionStatus.update { status -> status.copy(registeredListeners = 0) }
            doDisconnect()
        }
    }

    private fun doDisconnect() {
        logDebug<LiveChatWebSocket> { "Disconnecting live chat socket" }
        socketJob?.cancel()
    }

    override fun send(message: CharSequence, inReplyToId: String?) {}

    private suspend fun loadRecentMessages() {
        val prefs = preferencesRepository.currentPreferences.first()
        if (!prefs.enableRecentMessages) return

        try {
            _flow.emitAll(
                recentMessagesRepository
                    .loadRecentMessages(
                        channelLogin = channelLogin,
                        limit = AppPreferences.Defaults.RecentChatLimit,
                    )
                    .asFlow()
                    .filterIsInstance<IrcEvent.Message>()
                    .mapNotNull { event -> mapper.mapMessage(event) },
            )
        } catch (e: Exception) {
            logError<LiveChatWebSocket>(e) { "Failed to load recent messages for channel $channelLogin" }
        }
    }

    class Factory(
        private val clock: Clock,
        private val context: Context,
        private val networkStateObserver: NetworkStateObserver,
        private val parser: TwitchIrcCommandParser,
        private val mapper: IrcMessageMapper,
        private val recentMessagesRepository: RecentMessagesRepository,
        private val preferencesRepository: PreferenceRepository,
        private val httpClient: HttpClient,
    ) : ChatCommandHandlerFactory {

        override fun create(
            scope: CoroutineScope,
            channelLogin: String,
            channelId: String,
        ): LiveChatWebSocket {
            return LiveChatWebSocket(
                clock = clock,
                context = context,
                networkStateObserver = networkStateObserver,
                scope = scope,
                parser = parser,
                mapper = mapper,
                httpClient = httpClient,
                recentMessagesRepository = recentMessagesRepository,
                preferencesRepository = preferencesRepository,
                channelLogin = channelLogin,
            )
        }
    }
}
