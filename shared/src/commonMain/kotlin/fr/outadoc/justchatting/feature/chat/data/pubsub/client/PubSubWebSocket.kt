package fr.outadoc.justchatting.feature.chat.data.pubsub.client

import fr.outadoc.justchatting.feature.chat.data.pubsub.client.model.PubSubClientMessage
import fr.outadoc.justchatting.feature.chat.data.pubsub.client.model.PubSubServerMessage
import fr.outadoc.justchatting.feature.chat.domain.handler.ChatCommandHandlerFactory
import fr.outadoc.justchatting.feature.chat.domain.handler.ChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPluginsProvider
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.core.DispatchersProvider
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
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class PubSubWebSocket(
    private val networkStateObserver: NetworkStateObserver,
    private val httpClient: HttpClient,
    private val appUser: AppUser.LoggedIn,
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
                registeredListeners = 0,
            ),
        )

    override val connectionStatus = _connectionStatus.asStateFlow()

    override val eventFlow: Flow<ChatEvent> = channelFlow {
        _connectionStatus.update { it.copy(registeredListeners = 1) }
        try {
            networkStateObserver.state.collectLatest { netState ->
                if (netState is NetworkStateObserver.NetworkState.Available) {
                    logDebug<PubSubWebSocket> { "Network is available, listening" }
                    while (currentCoroutineContext().isActive) {
                        _connectionStatus.update { it.copy(isAlive = true) }
                        try {
                            listen()
                        } catch (e: Exception) {
                            logError<PubSubWebSocket>(e) { "Socket was closed" }
                        }
                        _connectionStatus.update { it.copy(isAlive = false) }
                        delayWithJitter(1.seconds, maxJitter = 3.seconds)
                    }
                } else {
                    logDebug<PubSubWebSocket> { "Network is out, waiting" }
                    _connectionStatus.update { it.copy(isAlive = false) }
                }
            }
        } finally {
            _connectionStatus.update { it.copy(registeredListeners = 0) }
        }
    }.flowOn(DispatchersProvider.io)

    private suspend fun ProducerScope<ChatEvent>.listen() {
        httpClient.webSocket(ENDPOINT) {
            logDebug<PubSubWebSocket> { "Socket open, sending the LISTEN message" }

            // Tell the server what we want to receive
            sendSerialized<PubSubClientMessage>(
                PubSubClientMessage.Listen(
                    data =
                    PubSubClientMessage.Listen.Data(
                        topics =
                        pubSubPluginsProvider
                            .get()
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
                handleMessage(receiveDeserialized()) { event ->
                    this@listen.send(event)
                }
            }
        }
    }

    private suspend fun DefaultWebSocketSession.handleMessage(
        received: PubSubServerMessage,
        emit: suspend (ChatEvent) -> Unit,
    ) {
        logInfo<PubSubWebSocket> { "received: $received" }

        when (received) {
            is PubSubServerMessage.Message -> {
                val plugin: PubSubPlugin<*>? =
                    plugins.firstOrNull { plugin ->
                        plugin.getTopic(channelId) == received.data.topic
                    }

                plugin?.apply {
                    for (event in parseMessage(received.data.message)) {
                        emit(event)
                    }
                }
            }

            is PubSubServerMessage.Response -> {
                if (received.error.isNotEmpty()) {
                    _connectionStatus.update { status -> status.copy(isAlive = false) }
                    close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, message = ""))
                }
            }

            PubSubServerMessage.Pong -> {}

            PubSubServerMessage.Reconnect -> {
                close(CloseReason(CloseReason.Codes.SERVICE_RESTART, message = ""))
            }
        }
    }

    class Factory(
        private val networkStateObserver: NetworkStateObserver,
        private val httpClient: HttpClient,
        private val pubSubPluginsProvider: PubSubPluginsProvider,
    ) : ChatCommandHandlerFactory {
        override fun create(
            channelLogin: String,
            channelId: String,
            appUser: AppUser.LoggedIn,
        ): PubSubWebSocket = PubSubWebSocket(
            networkStateObserver = networkStateObserver,
            httpClient = httpClient,
            appUser = appUser,
            pubSubPluginsProvider = pubSubPluginsProvider,
            channelId = channelId,
        )
    }
}
