package fr.outadoc.justchatting.feature.chat.data.irc

import fr.outadoc.justchatting.feature.chat.data.Defaults
import fr.outadoc.justchatting.feature.chat.domain.handler.ChatCommandHandlerFactory
import fr.outadoc.justchatting.feature.chat.domain.handler.ChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Logged in chat thread.
 *
 * Needed because user's own messages are only send when logged out. This thread handles
 * user-specific NOTICE and USERSTATE messages, and [LoggedInChatWebSocket] handles the rest.
 *
 * Use this class to write messages to the chat.
 */
internal class LoggedInChatWebSocket(
    networkStateObserver: NetworkStateObserver,
    private val scope: CoroutineScope,
    private val parser: TwitchIrcCommandParser,
    private val httpClient: HttpClient,
    private val authRepository: AuthRepository,
    private val channelLogin: String,
) : ChatEventHandler {

    companion object {
        private const val ENDPOINT = "wss://irc-ws.chat.twitch.tv"
    }

    private val _eventFlow = MutableSharedFlow<ChatEvent>(
        replay = Defaults.EventBufferSize,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val eventFlow: Flow<ChatEvent> = _eventFlow

    private val _connectionStatus: MutableStateFlow<ConnectionStatus> =
        MutableStateFlow(
            ConnectionStatus(
                isAlive = false,
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
        if (socketJob?.isActive == true) {
            return
        }

        socketJob = scope.launch(DispatchersProvider.io + SupervisorJob()) {
            logDebug<LoggedInChatWebSocket> { "Starting job" }

            _connectionStatus.update { status -> status.copy(registeredListeners = 1) }

            while (isActive) {
                if (isNetworkAvailable) {
                    logDebug<LoggedInChatWebSocket> { "Network is available, listening" }
                    _connectionStatus.update { status ->
                        status.copy(
                            isAlive = true,
                        )
                    }

                    try {
                        listen()
                    } catch (e: Exception) {
                        logError<LoggedInChatWebSocket>(e) { "Socket was closed" }
                    }
                } else {
                    logDebug<LoggedInChatWebSocket> { "Network is out, delay and retry" }
                    _connectionStatus.update { status ->
                        status.copy(
                            isAlive = false,
                        )
                    }
                }

                if (isActive) {
                    delayWithJitter(1.seconds, maxJitter = 3.seconds)
                }
            }
        }
    }

    private suspend fun listen() {
        httpClient.webSocket(ENDPOINT) {
            logDebug<LoggedInChatWebSocket> { "Socket open, logging in" }

            val appUser = authRepository.currentUser.first()

            if (appUser !is AppUser.LoggedIn) return@webSocket

            send("PASS oauth:${appUser.token}")
            send("NICK ${appUser.userLogin}")
            send("CAP REQ :twitch.tv/tags twitch.tv/commands")
            send("JOIN #$channelLogin")

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
        logInfo<LoggedInChatWebSocket> { "received: $received" }

        when (val command = parser.parse(received)) {
            is ChatEvent.Message.Notice -> {
                _eventFlow.emit(command)
            }

            is ChatEvent.Command.UserState -> {
                _eventFlow.emit(command)
            }

            is ChatEvent.Command.Ping -> {
                send("PONG :tmi.twitch.tv")
            }

            else -> {}
        }
    }

    override fun disconnect() {
        scope.launch {
            _connectionStatus.update { status -> status.copy(registeredListeners = 0) }
            doDisconnect()
        }
    }

    private fun doDisconnect() {
        logDebug<LoggedInChatWebSocket> { "Disconnecting logged in chat socket" }
        socketJob?.cancel()
    }

    class Factory(
        private val networkStateObserver: NetworkStateObserver,
        private val parser: TwitchIrcCommandParser,
        private val authRepository: AuthRepository,
        private val httpClient: HttpClient,
    ) : ChatCommandHandlerFactory {

        override fun create(
            scope: CoroutineScope,
            channelLogin: String,
            channelId: String,
        ): LoggedInChatWebSocket = LoggedInChatWebSocket(
            networkStateObserver = networkStateObserver,
            scope = scope,
            parser = parser,
            httpClient = httpClient,
            authRepository = authRepository,
            channelLogin = channelLogin,
        )
    }
}
