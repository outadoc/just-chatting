package fr.outadoc.justchatting.component.twitch.websocket.irc

import android.content.Context
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.ConnectionStatus
import fr.outadoc.justchatting.component.chatapi.common.handler.ChatCommandHandlerFactory
import fr.outadoc.justchatting.component.chatapi.common.handler.ChatEventHandler
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.component.twitch.R
import fr.outadoc.justchatting.component.twitch.websocket.Defaults
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.Command
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.PingCommand
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.UserState
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
import kotlinx.collections.immutable.toImmutableList
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
    private val context: Context,
    private val parser: TwitchIrcCommandParser,
    private val mapper: IrcEventMapper,
    private val httpClient: HttpClient,
    private val preferencesRepository: PreferenceRepository,
    private val channelLogin: String,
) : ChatEventHandler {

    companion object {
        const val ENDPOINT = "wss://irc-ws.chat.twitch.tv"
    }

    private val _flow = MutableSharedFlow<ChatEvent>(
        replay = Defaults.EventBufferSize,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val commandFlow: Flow<ChatEvent> = _flow

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
                                .filter { it.isNotBlank() }
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
            is Command.Notice -> {
                mapper.map(command)
                    ?.let { event -> _flow.emit(event) }
            }

            is UserState -> {
                _flow.emit(
                    ChatEvent.UserState(
                        emoteSets = command.emoteSets.toImmutableList()
                    )
                )
            }

            is PingCommand -> {
                send("PONG :tmi.twitch.tv")
            }

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
                    ChatEvent.Highlighted(
                        header = context.getString(
                            R.string.chat_send_msg_error,
                            e.toString(),
                        ),
                        data = null,
                        timestamp = clock.now(),
                    ),
                )
            }
        }
    }

    class Factory(
        private val clock: Clock,
        private val networkStateObserver: NetworkStateObserver,
        private val parser: TwitchIrcCommandParser,
        private val mapper: IrcEventMapper,
        private val preferencesRepository: PreferenceRepository,
        private val httpClient: HttpClient,
        private val context: Context,
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
                mapper = mapper,
                httpClient = httpClient,
                preferencesRepository = preferencesRepository,
                context = context,
                channelLogin = channelLogin,
            )
        }
    }
}
