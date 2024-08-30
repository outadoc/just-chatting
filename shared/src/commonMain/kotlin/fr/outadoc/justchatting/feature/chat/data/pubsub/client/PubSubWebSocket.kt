package fr.outadoc.justchatting.feature.chat.data.pubsub.client

import fr.outadoc.justchatting.feature.chat.data.pubsub.client.model.PubSubClientMessage
import fr.outadoc.justchatting.feature.chat.data.pubsub.client.model.PubSubServerMessage
import fr.outadoc.justchatting.feature.chat.domain.handler.ChatCommandHandlerFactory
import fr.outadoc.justchatting.feature.chat.domain.handler.ChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPluginsProvider
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import fr.outadoc.justchatting.utils.core.delayWithJitter
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.logging.logInfo
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class PubSubWebSocket(
    private val networkStateObserver: NetworkStateObserver,
    private val scope: CoroutineScope,
    private val httpClient: HttpClient,
    private val authRepository: AuthRepository,
    private val pubSubPluginsProvider: PubSubPluginsProvider,
    private val channelId: String,
) : ChatEventHandler {

    companion object {
        private const val ENDPOINT = "wss://pubsub-edge.twitch.tv"
    }

    private val plugins = pubSubPluginsProvider.get()

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

    init {
        scope.launch {
            networkStateObserver.state.collectLatest { state ->
                isNetworkAvailable = state is NetworkStateObserver.NetworkState.Available
            }
        }
    }

    override val eventFlow: Flow<ChatEvent> =
        flow {
            logDebug<PubSubWebSocket> { "Starting job" }

            _connectionStatus.update { status -> status.copy(registeredListeners = 1) }

            while (true) {
                if (isNetworkAvailable) {
                    logDebug<PubSubWebSocket> { "Network is available, listening" }
                    _connectionStatus.update { status -> status.copy(isAlive = true) }

                    try {
                        listen()
                    } catch (e: Exception) {
                        logError<PubSubWebSocket>(e) { "Socket was closed" }
                    }
                } else {
                    logDebug<PubSubWebSocket> { "Network is out, delay and retry" }
                    _connectionStatus.update { status -> status.copy(isAlive = false) }
                }

                delayWithJitter(1.seconds, maxJitter = 3.seconds)
            }
        }.onCompletion {
            _connectionStatus.update { status -> status.copy(registeredListeners = 0) }
        }

    private suspend fun FlowCollector<ChatEvent>.listen() {
        httpClient.webSocket(ENDPOINT) {
            val appUser = authRepository.currentUser.first()

            if (appUser !is AppUser.LoggedIn) return@webSocket

            logDebug<PubSubWebSocket> { "Socket open, sending the LISTEN message" }

            // Tell the server what we want to receive
            sendSerialized<PubSubClientMessage>(
                PubSubClientMessage.Listen(
                    data = PubSubClientMessage.Listen.Data(
                        topics = pubSubPluginsProvider.get()
                            .map { plugin -> plugin.getTopic(channelId) },
                        authToken = appUser.token,
                    ),
                ),
            )

            logDebug<PubSubWebSocket> { "Sent LISTEN message" }

            // Send PING from time to time
            launch {
                while (isActive) {
                    logDebug<PubSubWebSocket> { "Sending PING" }
                    sendSerialized(PubSubClientMessage.Ping)
                    delayWithJitter(4.minutes, maxJitter = 30.seconds)
                }
            }

            // Receive messages
            while (isActive) {
                handleMessage(
                    session = this,
                    received = receiveDeserialized()
                )
            }
        }
    }

    private suspend fun FlowCollector<ChatEvent>.handleMessage(
        session: DefaultWebSocketSession,
        received: PubSubServerMessage
    ) {
        logInfo<PubSubWebSocket> { "received: $received" }

        when (received) {
            is PubSubServerMessage.Message -> {
                val plugin: PubSubPlugin<*>? =
                    plugins.firstOrNull { plugin ->
                        plugin.getTopic(channelId) == received.data.topic
                    }

                plugin?.apply {
                    emitAll(
                        parseMessage(received.data.message).asFlow(),
                    )
                }
            }

            is PubSubServerMessage.Response -> {
                if (received.error.isNotEmpty()) {
                    _connectionStatus.update { status -> status.copy(isAlive = false) }
                    session.close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, message = ""))
                }
            }

            PubSubServerMessage.Pong -> {}

            PubSubServerMessage.Reconnect -> {
                session.close(CloseReason(CloseReason.Codes.SERVICE_RESTART, message = ""))
            }
        }
    }

    class Factory(
        private val networkStateObserver: NetworkStateObserver,
        private val httpClient: HttpClient,
        private val authRepository: AuthRepository,
        private val pubSubPluginsProvider: PubSubPluginsProvider,
    ) : ChatCommandHandlerFactory {

        override fun create(
            scope: CoroutineScope,
            channelLogin: String,
            channelId: String,
        ): PubSubWebSocket = PubSubWebSocket(
            networkStateObserver = networkStateObserver,
            scope = scope,
            httpClient = httpClient,
            authRepository = authRepository,
            pubSubPluginsProvider = pubSubPluginsProvider,
            channelId = channelId,
        )
    }
}
