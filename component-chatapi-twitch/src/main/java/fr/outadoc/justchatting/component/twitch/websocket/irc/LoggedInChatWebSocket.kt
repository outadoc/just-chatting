package fr.outadoc.justchatting.component.twitch.websocket.irc

import android.content.Context
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.ConnectionStatus
import fr.outadoc.justchatting.component.chatapi.common.handler.ChatCommandHandlerFactory
import fr.outadoc.justchatting.component.chatapi.common.handler.ChatEventHandler
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.component.twitch.R
import fr.outadoc.justchatting.component.twitch.websocket.Defaults
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.IrcEvent
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
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
    private val mapper: IrcMessageMapper,
    private val httpClient: HttpClient,
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

    private data class QueuedMessage(
        val authoringTime: Instant,
        val rawMessage: String,
    )

    private val messageToSend: MutableStateFlow<QueuedMessage?> = MutableStateFlow(null)
    private val messageMaxRetryTimeout: Duration = 10.seconds

    private val _connectionStatus: MutableStateFlow<ConnectionStatus> =
        MutableStateFlow(
            ConnectionStatus(
                isAlive = false,
                preventSendingMessages = true,
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
            logDebug<LoggedInChatWebSocket> { "Starting job" }

            _connectionStatus.update { status -> status.copy(registeredListeners = 1) }

            while (isActive) {
                if (isNetworkAvailable) {
                    logDebug<LoggedInChatWebSocket> { "Network is available, listening" }
                    _connectionStatus.update { status ->
                        status.copy(
                            isAlive = true,
                            preventSendingMessages = false,
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
                            preventSendingMessages = true,
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

            val prefs = preferencesRepository.currentPreferences.first()

            send("PASS oauth:${prefs.appUser.helixToken}")
            send("NICK ${prefs.appUser.login}")
            send("CAP REQ :twitch.tv/tags twitch.tv/commands")
            send("JOIN #$channelLogin")

            launch {
                messageToSend
                    .filterNotNull()
                    .collect { message ->
                        if (isActive) {
                            val shouldAttemptToSend: Boolean =
                                clock.now() < message.authoringTime + messageMaxRetryTimeout

                            if (shouldAttemptToSend) {
                                logDebug<LoggedInChatWebSocket> { "Sending PRIVMSG: ${message.rawMessage}" }

                                // Try sending message
                                send(message.rawMessage)

                                logDebug<LoggedInChatWebSocket> { "Sent message" }
                            } else {
                                // We've been trying to send this message for a while now, give up
                                logError<LoggedInChatWebSocket> { "Timeout while trying to send message: $message" }

                                _flow.emit(
                                    ChatEvent.Message.Highlighted(
                                        header = context.getString(R.string.chat_send_msg_error),
                                        body = null,
                                        timestamp = clock.now(),
                                    ),
                                )
                            }

                            // Remove message from the queue, if we sent it successfully or cleared it from the queue
                            this@LoggedInChatWebSocket.messageToSend.value = null
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
        }
    }

    private suspend fun DefaultWebSocketSession.handleMessage(received: String) {
        logInfo<LoggedInChatWebSocket> { "received: $received" }

        when (val command = parser.parse(received)) {
            is IrcEvent.Message.Notice -> {
                _flow.emit(mapper.mapMessage(command))
            }

            is IrcEvent.Command.UserState -> {
                _flow.emit(
                    ChatEvent.UserState(
                        emoteSets = command.emoteSets.toImmutableList(),
                    ),
                )
            }

            is IrcEvent.Command.Ping -> {
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

    override fun send(message: CharSequence, inReplyToId: String?) {
        scope.launch {
            val inReplyToPrefix = inReplyToId?.let { id -> "@reply-parent-msg-id=$id " } ?: ""
            val privMsg = "${inReplyToPrefix}PRIVMSG #$channelLogin :$message"

            logDebug<LoggedInChatWebSocket> { "Queuing message to #$channelLogin, in reply to $inReplyToId: $message" }

            messageToSend.emit(
                QueuedMessage(
                    authoringTime = clock.now(),
                    rawMessage = privMsg,
                ),
            )
        }
    }

    class Factory(
        private val clock: Clock,
        private val networkStateObserver: NetworkStateObserver,
        private val parser: TwitchIrcCommandParser,
        private val mapper: IrcMessageMapper,
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
