package fr.outadoc.justchatting.feature.chat.data.websocket.irc

import fr.outadoc.justchatting.component.preferences.data.AppPreferences
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.chat.data.ChatCommandHandler
import fr.outadoc.justchatting.feature.chat.data.ChatCommandHandlerFactory
import fr.outadoc.justchatting.feature.chat.data.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import fr.outadoc.justchatting.feature.chat.data.model.Command
import fr.outadoc.justchatting.feature.chat.data.model.PingCommand
import fr.outadoc.justchatting.feature.chat.data.model.UserState
import fr.outadoc.justchatting.feature.chat.data.parser.ChatMessageParser
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

/**
 * Logged in chat thread.
 *
 * Needed because user's own messages are only send when logged out. This thread handles
 * user-specific NOTICE and USERSTATE messages, and [LoggedInChatWebSocket] handles the rest.
 *
 * Use this class to write messages to the chat.
 */
class LoggedInChatWebSocket(
    networkStateObserver: NetworkStateObserver,
    private val scope: CoroutineScope,
    private val clock: Clock,
    private val parser: ChatMessageParser,
    private val httpClient: HttpClient,
    private val preferencesRepository: PreferenceRepository,
    private val channelLogin: String,
) : ChatCommandHandler {

    companion object {
        const val ENDPOINT = "wss://irc-ws.chat.twitch.tv"
    }

    private val _flow = MutableSharedFlow<ChatCommand>(
        replay = AppPreferences.Defaults.ChatLimitRange.last,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val commandFlow: Flow<ChatCommand> = _flow

    private val messagesToSend = MutableSharedFlow<String>()

    private val _connectionStatus: MutableStateFlow<ConnectionStatus> =
        MutableStateFlow(
            ConnectionStatus(
                isAlive = false,
                preventSendingMessages = false,
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
            logDebug<LoggedInChatWebSocket> { "Starting job" }

            while (isActive) {
                if (isNetworkAvailable) {
                    logDebug<LoggedInChatWebSocket> { "Network is available, listening" }
                    _connectionStatus.update { status -> status.copy(isAlive = true) }
                    listen()
                } else {
                    logDebug<LoggedInChatWebSocket> { "Network is out, delay and retry" }
                    _connectionStatus.update { status -> status.copy(isAlive = false) }
                }

                delayWithJitter(1.seconds, maxJitter = 3.seconds)
            }
        }
    }

    private suspend fun listen() {
        httpClient.webSocket(ENDPOINT) {
            try {
                logDebug<LoggedInChatWebSocket> { "Socket open, logging in" }

                val prefs = preferencesRepository.currentPreferences.first()
                send("PASS oauth:${prefs.appUser.helixToken}")
                send("NICK ${prefs.appUser.login}")
                send("CAP REQ :twitch.tv/tags twitch.tv/commands")
                send("JOIN #$channelLogin")

                launch {
                    messagesToSend.collect { message ->
                        if (isActive) {
                            logDebug<LoggedInChatWebSocket> { "Sending PRIMSG: $message" }
                            send(message)
                            logDebug<LoggedInChatWebSocket> { "Sent message" }
                        }
                    }
                }

                // Receive messages
                while (isActive) {
                    when (val received = incoming.receive()) {
                        is Frame.Text -> {
                            received.readText()
                                .lines()
                                .forEach { line -> handleMessage(line) }
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                logError<LoggedInChatWebSocket>(e) { "Socket was closed" }
            }
        }
    }

    private suspend fun DefaultWebSocketSession.handleMessage(received: String) {
        logInfo<LoggedInChatWebSocket> { "received: $received" }

        when (val command = parser.parse(received)) {
            is Command.Notice, is UserState -> _flow.emit(command)
            is PingCommand -> send("PONG :tmi.twitch.tv")
            else -> {}
        }
    }

    override fun disconnect() {
        scope.launch {
            doDisconnect()
        }
    }

    private fun doDisconnect() {
        logDebug<LoggedInChatWebSocket> { "Disconnecting logged in chat socket" }
        socketJob?.cancel()
    }

    override fun send(message: CharSequence, inReplyToId: String?) {
        scope.launch {
            try {
                val inReplyToPrefix = inReplyToId?.let { id -> "@reply-parent-msg-id=$id " } ?: ""
                val privMsg = "${inReplyToPrefix}PRIVMSG #$channelLogin :$message"

                logDebug<LoggedInChatWebSocket> { "Queuing message to #$channelLogin, in reply to $inReplyToId: $message" }

                messagesToSend.emit(privMsg)
            } catch (e: IOException) {
                logError<LoggedInChatWebSocket>(e) { "Error sending message" }
                _flow.emit(
                    Command.SendMessageError(
                        throwable = e,
                        timestamp = clock.now(),
                    ),
                )
            }
        }
    }

    class Factory(
        private val clock: Clock,
        private val networkStateObserver: NetworkStateObserver,
        private val parser: ChatMessageParser,
        private val preferencesRepository: PreferenceRepository,
        private val httpClient: HttpClient,
    ) : ChatCommandHandlerFactory {

        override fun create(
            scope: CoroutineScope,
            channelLogin: String,
            channelId: String,
        ): LoggedInChatWebSocket {
            return LoggedInChatWebSocket(
                clock = clock,
                networkStateObserver = networkStateObserver,
                scope = scope,
                parser = parser,
                httpClient = httpClient,
                preferencesRepository = preferencesRepository,
                channelLogin = channelLogin,
            )
        }
    }
}
