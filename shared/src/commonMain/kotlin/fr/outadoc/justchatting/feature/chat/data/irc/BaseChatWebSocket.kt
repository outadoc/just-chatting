package fr.outadoc.justchatting.feature.chat.data.irc

import fr.outadoc.justchatting.feature.chat.domain.handler.ChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Base class for Twitch IRC websocket connections.
 *
 * Waits for the network to be available, then keeps a websocket connection open,
 * reconnecting with jitter when it drops or goes silent for too long. Received frames
 * are parsed into [ChatEvent]s; PING commands are answered here, everything else is
 * passed to the [Session] provided by the subclass.
 */
internal abstract class BaseChatWebSocket(
    private val networkStateObserver: NetworkStateObserver,
    private val parser: TwitchIrcCommandParser,
    private val httpClient: HttpClient,
    private val dispatchersProvider: DispatchersProvider,
    private val endpoint: String,
    private val messageTimeout: Duration,
) : ChatEventHandler {
    companion object {
        internal const val DEFAULT_ENDPOINT = "wss://irc-ws.chat.twitch.tv"

        // Twitch pings us about every five minutes; if we go longer than this
        // without receiving anything, assume the connection is half-open.
        internal val DEFAULT_MESSAGE_TIMEOUT = 6.minutes
    }

    protected abstract val logTag: String

    private val _connectionStatus: MutableStateFlow<ConnectionStatus> =
        MutableStateFlow(
            ConnectionStatus(
                isAlive = false,
                registeredListeners = 0,
            ),
        )

    override val connectionStatus = _connectionStatus.asStateFlow()

    /**
     * Callbacks describing one collection of the event flow. [onConnected] is invoked
     * once per established connection, [onCommandReceived] for every parsed command
     * except PING.
     */
    protected class Session(
        val onConnected: suspend Connection.() -> Unit,
        val onCommandReceived: suspend Connection.(ChatEvent) -> Unit,
    )

    /**
     * An established websocket connection, from which commands can be sent to the
     * server and events emitted to the collector.
     */
    protected class Connection internal constructor(
        private val webSocketSession: DefaultWebSocketSession,
        private val producer: ProducerScope<ChatEvent>,
    ) {
        suspend fun sendCommand(command: String) {
            webSocketSession.send(command)
        }

        suspend fun emit(event: ChatEvent) {
            producer.send(event)
        }
    }

    protected fun chatEventFlow(createSession: () -> Session): Flow<ChatEvent> = channelFlow {
        val session = createSession()
        _connectionStatus.update { it.copy(registeredListeners = it.registeredListeners + 1) }
        try {
            networkStateObserver.state.collectLatest { netState ->
                if (netState is NetworkStateObserver.NetworkState.Available) {
                    logDebug(logTag) { "Network is available, listening" }
                    while (currentCoroutineContext().isActive) {
                        try {
                            connect(session)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            logError(logTag, e) { "Socket was closed" }
                        }
                        delayWithJitter(1.seconds, maxJitter = 3.seconds)
                    }
                } else {
                    logDebug(logTag) { "Network is out, waiting" }
                    _connectionStatus.update { it.copy(isAlive = false) }
                }
            }
        } finally {
            _connectionStatus.update { it.copy(registeredListeners = it.registeredListeners - 1) }
        }
    }.flowOn(dispatchersProvider.io)

    private suspend fun ProducerScope<ChatEvent>.connect(session: Session) {
        httpClient.webSocket(endpoint) {
            logDebug(logTag) { "Socket open" }
            _connectionStatus.update { it.copy(isAlive = true) }
            try {
                val connection = Connection(this, this@connect)
                session.onConnected(connection)

                // Receive messages
                while (isActive) {
                    val received =
                        try {
                            withTimeout(messageTimeout) { incoming.receive() }
                        } catch (e: TimeoutCancellationException) {
                            logError(logTag, e) { "No message received in $messageTimeout, closing socket" }
                            break
                        }

                    when (received) {
                        is Frame.Text -> {
                            received
                                .readText()
                                .lines()
                                .filter { it.isNotBlank() }
                                .forEach { line -> handleLine(connection, session, line) }
                        }

                        else -> {}
                    }
                }
            } finally {
                _connectionStatus.update { it.copy(isAlive = false) }
            }
        }
    }

    private suspend fun handleLine(
        connection: Connection,
        session: Session,
        line: String,
    ) {
        logInfo(logTag) { "received: $line" }

        when (val command = parser.parse(line)) {
            is ChatEvent.Command.Ping -> connection.sendCommand("PONG :tmi.twitch.tv")
            null -> {}
            else -> session.onCommandReceived(connection, command)
        }
    }
}
