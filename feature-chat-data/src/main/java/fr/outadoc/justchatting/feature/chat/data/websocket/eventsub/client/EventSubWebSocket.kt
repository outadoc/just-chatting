package fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.client

import fr.outadoc.justchatting.component.preferences.data.AppPreferences
import fr.outadoc.justchatting.component.twitch.api.HelixApi
import fr.outadoc.justchatting.feature.chat.data.ChatCommandHandler
import fr.outadoc.justchatting.feature.chat.data.ChatCommandHandlerFactory
import fr.outadoc.justchatting.feature.chat.data.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.client.model.EventSubMessageWithMetadata
import fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.plugin.EventSubPlugin
import fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.plugin.EventSubPluginsProvider
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import fr.outadoc.justchatting.utils.core.delayWithJitter
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.logging.logInfo
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

class EventSubWebSocket(
    private val networkStateObserver: NetworkStateObserver,
    private val scope: CoroutineScope,
    private val httpClient: HttpClient,
    private val helixApi: HelixApi,
    private val eventSubPluginsProvider: EventSubPluginsProvider,
    private val json: Json,
    private val channelId: String,
) : ChatCommandHandler {

    companion object {
        const val DEFAULT_ENDPOINT = "wss://eventsub-beta.wss.twitch.tv/ws"
    }

    private val plugins = eventSubPluginsProvider.get()

    private val _flow = MutableSharedFlow<ChatCommand>(
        replay = AppPreferences.Defaults.ChatLimitRange.last,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val commandFlow: Flow<ChatCommand> = _flow

    private val _connectionStatus: MutableStateFlow<ConnectionStatus> =
        MutableStateFlow(
            ConnectionStatus(
                isAlive = false,
                preventSendingMessages = false,
            ),
        )

    override val connectionStatus = _connectionStatus.asStateFlow()

    private var endpointUrl: String = DEFAULT_ENDPOINT
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
            logDebug<EventSubWebSocket> { "Starting job" }

            while (isActive) {
                if (isNetworkAvailable) {
                    logDebug<EventSubWebSocket> { "Network is available, listening" }
                    _connectionStatus.update { status -> status.copy(isAlive = true) }
                    listen()
                } else {
                    logDebug<EventSubWebSocket> { "Network is out, delay and retry" }
                    _connectionStatus.update { status -> status.copy(isAlive = false) }
                }

                delayWithJitter(1.seconds, maxJitter = 3.seconds)
            }
        }
    }

    private suspend fun listen() {
        httpClient.webSocket(endpointUrl) {
            try {
                logDebug<EventSubWebSocket> { "Socket open, waiting for welcome" }

                // Receive messages
                while (isActive) {
                    when (val frame = incoming.receive()) {
                        is Frame.Text -> handleMessage(frame.readText())
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                logError<EventSubWebSocket>(e) { "Socket was closed" }
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.handleMessage(message: String) {
        val received = json.decodeFromString<EventSubMessageWithMetadata>(message)

        logInfo<EventSubWebSocket> { "received: $message" }
        logInfo<EventSubWebSocket> { "received decoded: $received" }

        when (received.metadata) {
            is EventSubMessageWithMetadata.Metadata.Welcome -> {
                received.payload.session?.id?.let { sessionId ->
                    plugins.map { plugin -> plugin.subscriptionType }
                        .forEach { subscriptionType ->
                            helixApi.createSubscription(
                                type = subscriptionType,
                                channelId = channelId,
                                sessionId = sessionId
                            )
                        }
                }
            }

            is EventSubMessageWithMetadata.Metadata.Notification -> {
                val plugin: EventSubPlugin<*>? = plugins.firstOrNull { plugin ->
                    plugin.subscriptionType == received.metadata.subscriptionType
                }

                plugin?.parseMessage(message)
                    ?.let { parsed ->
                        _flow.emit(parsed)
                    }
            }

            is EventSubMessageWithMetadata.Metadata.Reconnect -> {
                if (received.payload.session?.reconnectUrl != null) {
                    endpointUrl = received.payload.session.reconnectUrl
                }

                close(CloseReason(CloseReason.Codes.SERVICE_RESTART, message = ""))
            }

            is EventSubMessageWithMetadata.Metadata.KeepAlive -> {}
            is EventSubMessageWithMetadata.Metadata.Revocation -> {}
        }
    }

    override fun disconnect() {
        scope.launch {
            doDisconnect()
        }
    }

    private fun doDisconnect() {
        logDebug<EventSubWebSocket> { "Disconnecting socket" }
        socketJob?.cancel()
    }

    override fun send(message: CharSequence, inReplyToId: String?) {}

    class Factory(
        private val networkStateObserver: NetworkStateObserver,
        private val httpClient: HttpClient,
        private val helixApi: HelixApi,
        private val eventSubPluginsProvider: EventSubPluginsProvider,
        private val json: Json
    ) : ChatCommandHandlerFactory {

        override fun create(
            scope: CoroutineScope,
            channelLogin: String,
            channelId: String,
        ): EventSubWebSocket {
            return EventSubWebSocket(
                networkStateObserver = networkStateObserver,
                scope = scope,
                httpClient = httpClient,
                helixApi = helixApi,
                eventSubPluginsProvider = eventSubPluginsProvider,
                json = json,
                channelId = channelId,
            )
        }
    }
}
