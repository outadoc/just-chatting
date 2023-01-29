package fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.client

import fr.outadoc.justchatting.component.preferences.data.AppPreferences
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.chat.data.ChatCommandHandler
import fr.outadoc.justchatting.feature.chat.data.ChatCommandHandlerFactory
import fr.outadoc.justchatting.feature.chat.data.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.client.model.EventSubClientMessage
import fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.client.model.EventSubServerMessage
import fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.plugin.EventSubPlugin
import fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.plugin.EventSubPluginsProvider
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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class EventSubWebSocket(
    private val networkStateObserver: NetworkStateObserver,
    private val scope: CoroutineScope,
    private val httpClient: HttpClient,
    private val preferencesRepository: PreferenceRepository,
    private val eventSubPluginsProvider: EventSubPluginsProvider,
    private val channelId: String,
) : ChatCommandHandler {

    companion object {
        const val ENDPOINT = "wss://eventsub-beta.wss.twitch.tv/ws"
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
                    delayWithJitter(1.seconds, maxJitter = 3.seconds)
                }
            }
        }
    }

    private suspend fun listen() {
        httpClient.webSocket(ENDPOINT) {
            try {
                val helixToken: String =
                    preferencesRepository.currentPreferences.first().appUser.helixToken
                        ?: error("User is not authenticated")

                logDebug<EventSubWebSocket> { "Socket open, sending the LISTEN message" }

                // Tell the server what we want to receive
                sendSerialized<EventSubClientMessage>(
                    EventSubClientMessage.Listen(
                        data = EventSubClientMessage.Listen.Data(
                            topics = eventSubPluginsProvider.get()
                                .map { plugin -> plugin.getTopic(channelId) },
                            authToken = helixToken,
                        ),
                    ),
                )

                logDebug<EventSubWebSocket> { "Sent LISTEN message" }

                // Send PING from time to time
                launch {
                    while (isActive) {
                        logDebug<EventSubWebSocket> { "Sending PING" }
                        sendSerialized(EventSubClientMessage.Ping)
                        delayWithJitter(4.minutes, maxJitter = 30.seconds)
                    }
                }

                // Receive messages
                while (isActive) {
                    handleMessage(receiveDeserialized())
                }
            } catch (e: Exception) {
                logError<EventSubWebSocket>(e) { "Socket was closed" }
            }
        }
    }

    private suspend fun DefaultWebSocketSession.handleMessage(received: EventSubServerMessage) {
        logInfo<EventSubWebSocket> { "received: $received" }

        when (received) {
            is EventSubServerMessage.Message -> {
                val plugin: EventSubPlugin<*>? = plugins.firstOrNull { plugin ->
                    plugin.getTopic(channelId) == received.data.topic
                }

                plugin?.parseMessage(received.data.message)
                    ?.let { parsed ->
                        _flow.emit(parsed)
                    }
            }

            is EventSubServerMessage.Response -> {
                if (received.error.isNotEmpty()) {
                    _connectionStatus.update { status -> status.copy(isAlive = false) }
                    close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, message = ""))
                }
            }

            EventSubServerMessage.Pong -> {}

            EventSubServerMessage.Reconnect -> {
                close(CloseReason(CloseReason.Codes.SERVICE_RESTART, message = ""))
            }
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
        private val preferencesRepository: PreferenceRepository,
        private val eventSubPluginsProvider: EventSubPluginsProvider,
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
                preferencesRepository = preferencesRepository,
                eventSubPluginsProvider = eventSubPluginsProvider,
                channelId = channelId,
            )
        }
    }
}
