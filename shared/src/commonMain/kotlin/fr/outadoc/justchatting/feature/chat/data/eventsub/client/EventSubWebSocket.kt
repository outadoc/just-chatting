package fr.outadoc.justchatting.feature.chat.data.eventsub.client

import fr.outadoc.justchatting.feature.chat.data.eventsub.client.model.EventSubNotificationPayload
import fr.outadoc.justchatting.feature.chat.data.eventsub.client.model.EventSubServerMessage
import fr.outadoc.justchatting.feature.chat.data.eventsub.client.model.EventSubSessionPayload
import fr.outadoc.justchatting.feature.chat.data.http.EventSubSubscriptionRequest
import fr.outadoc.justchatting.feature.chat.domain.eventsub.EventSubPlugin
import fr.outadoc.justchatting.feature.chat.domain.eventsub.EventSubPluginsProvider
import fr.outadoc.justchatting.feature.chat.domain.handler.ChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.data.TwitchClient
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import fr.outadoc.justchatting.utils.core.delayWithJitter
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.logging.logInfo
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Receives chat messages and channel events from Twitch EventSub over a WebSocket.
 *
 * Only subscription types that a regular (non-moderator) user token can create are used.
 * Subscriptions are best-effort: the ones that fail (missing scope, cost limit reached
 * because several chats are open at once, etc.) are skipped.
 *
 * @see <a href="https://dev.twitch.tv/docs/eventsub/handling-websocket-events/">Handling WebSocket events</a>
 */
internal class EventSubWebSocket(
    private val networkStateObserver: NetworkStateObserver,
    private val httpClient: HttpClient,
    private val twitchClient: TwitchClient,
    private val json: Json,
    private val clock: Clock,
    eventSubPluginsProvider: EventSubPluginsProvider,
    private val dispatchersProvider: DispatchersProvider,
) : ChatEventHandler {
    private companion object {
        const val ENDPOINT = "wss://eventsub.wss.twitch.tv/ws?keepalive_timeout_seconds=30"
        val DEFAULT_KEEPALIVE_TIMEOUT = 30.seconds

        /**
         * Extra time we wait for a message past the keepalive timeout before
         * considering the connection dead.
         */
        val KEEPALIVE_GRACE_PERIOD = 10.seconds

        const val MAX_REMEMBERED_NOTIFICATION_IDS = 100
        const val MAX_REMEMBERED_CHAT_MESSAGE_IDS = AppPreferences.Defaults.RecentChatLimit * 2
    }

    private sealed interface SessionOutcome {
        /**
         * The connection was lost; start over with a new session.
         */
        data object Disconnected : SessionOutcome

        /**
         * Twitch asked us to move to another URL. Subscriptions carry over to the new session.
         */
        data class Reconnect(
            val url: String,
        ) : SessionOutcome

        /**
         * None of the subscriptions could be created, so there is nothing to listen to.
         */
        data object NoSubscriptions : SessionOutcome
    }

    /**
     * State shared between reconnections of a single collection of the event flow.
     */
    private class CollectionState {
        /**
         * Twitch may resend notifications; remember the ones we've already handled.
         */
        val seenNotificationIds = ArrayDeque<String>()

        /**
         * Chat messages may be both backfilled and received live; remember the ones
         * we've already emitted.
         */
        val seenChatMessageIds = ArrayDeque<String>()

        var lastMessageReceivedAt: Instant? = null
    }

    private val plugins: List<EventSubPlugin> = eventSubPluginsProvider.get()

    private val _connectionStatus: MutableStateFlow<ConnectionStatus> = MutableStateFlow(ConnectionStatus())

    override val connectionStatus: Flow<ConnectionStatus> = _connectionStatus.asStateFlow()

    // This socket is a singleton, but every collector of the event flow holds its own
    // websocket connection. isAlive is derived from connection counts, so that one
    // channel's live connection can't mask another channel's dead one.
    private fun updateConnectionStatus(transform: (ConnectionStatus) -> ConnectionStatus) {
        _connectionStatus.update { current ->
            val next = transform(current)
            next.copy(
                isAlive = next.aliveConnections > 0 && next.aliveConnections >= next.registeredListeners,
            )
        }
    }

    override fun getEventFlow(
        channelId: String,
        channelLogin: String,
        appUser: AppUser.LoggedIn,
    ): Flow<ChatEvent> =
        channelFlow {
            val state = CollectionState()
            updateConnectionStatus { it.copy(registeredListeners = it.registeredListeners + 1) }
            try {
                networkStateObserver.state.collectLatest { netState ->
                    if (netState !is NetworkStateObserver.NetworkState.Available) {
                        logDebug<EventSubWebSocket> { "Network is out, waiting" }
                        return@collectLatest
                    }

                    logDebug<EventSubWebSocket> { "Network is available, connecting" }

                    var url = ENDPOINT
                    var isNewSession = true

                    while (currentCoroutineContext().isActive) {
                        val outcome =
                            try {
                                listen(
                                    url = url,
                                    isNewSession = isNewSession,
                                    channelId = channelId,
                                    channelLogin = channelLogin,
                                    appUser = appUser,
                                    state = state,
                                )
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                logError<EventSubWebSocket>(e) { "Socket was closed" }
                                SessionOutcome.Disconnected
                            }

                        when (outcome) {
                            is SessionOutcome.Reconnect -> {
                                logInfo<EventSubWebSocket> { "Reconnecting to ${outcome.url}" }
                                url = outcome.url
                                isNewSession = false
                            }

                            SessionOutcome.Disconnected -> {
                                url = ENDPOINT
                                isNewSession = true
                                delayWithJitter(1.seconds, maxJitter = 3.seconds)
                            }

                            SessionOutcome.NoSubscriptions -> {
                                logInfo<EventSubWebSocket> { "No subscriptions were created, giving up" }
                                awaitCancellation()
                            }
                        }
                    }
                }
            } finally {
                updateConnectionStatus { it.copy(registeredListeners = it.registeredListeners - 1) }
            }
        }.flowOn(dispatchersProvider.io)

    private suspend fun ProducerScope<ChatEvent>.listen(
        url: String,
        isNewSession: Boolean,
        channelId: String,
        channelLogin: String,
        appUser: AppUser.LoggedIn,
        state: CollectionState,
    ): SessionOutcome {
        var outcome: SessionOutcome = SessionOutcome.Disconnected
        var isAlive = false

        suspend fun emit(
            event: ChatEvent,
            isBackfill: Boolean,
        ) {
            if (shouldEmit(event, state, isBackfill)) {
                send(event)
            }
        }

        httpClient.webSocket(url) {
            try {
                var keepaliveTimeout: Duration = DEFAULT_KEEPALIVE_TIMEOUT

                while (isActive) {
                    val message: EventSubServerMessage =
                        receiveMessage(timeout = keepaliveTimeout + KEEPALIVE_GRACE_PERIOD)
                            ?: run {
                                logInfo<EventSubWebSocket> { "No message received before keepalive timeout" }
                                return@webSocket
                            }

                    if (!state.seenNotificationIds.remember(message.metadata.messageId, MAX_REMEMBERED_NOTIFICATION_IDS)) {
                        logDebug<EventSubWebSocket> { "Ignoring duplicate message ${message.metadata.messageId}" }
                        continue
                    }

                    when (message.metadata.messageType) {
                        EventSubServerMessage.TYPE_WELCOME -> {
                            val session =
                                json
                                    .decodeFromJsonElement(EventSubSessionPayload.serializer(), message.payload)
                                    .session

                            session.keepaliveTimeoutSeconds?.let { timeout ->
                                keepaliveTimeout = timeout.seconds
                            }

                            if (isNewSession) {
                                val subscribedPlugins =
                                    subscribe(
                                        sessionId = session.id,
                                        channelId = channelId,
                                        appUser = appUser,
                                    )

                                if (subscribedPlugins.isEmpty()) {
                                    outcome = SessionOutcome.NoSubscriptions
                                    return@webSocket
                                }

                                isAlive = true
                                updateConnectionStatus { it.copy(aliveConnections = it.aliveConnections + 1) }

                                // Notifications received meanwhile wait in the socket's buffer
                                getInitialEvents(subscribedPlugins, channelId, channelLogin, appUser)
                                    .forEach { event -> emit(event, isBackfill = true) }
                            } else if (!isAlive) {
                                isAlive = true
                                updateConnectionStatus { it.copy(aliveConnections = it.aliveConnections + 1) }
                            }
                        }

                        EventSubServerMessage.TYPE_KEEPALIVE -> {}

                        EventSubServerMessage.TYPE_NOTIFICATION -> {
                            parseNotification(message).forEach { event ->
                                emit(event, isBackfill = false)
                            }
                        }

                        EventSubServerMessage.TYPE_RECONNECT -> {
                            val reconnectUrl =
                                json
                                    .decodeFromJsonElement(EventSubSessionPayload.serializer(), message.payload)
                                    .session
                                    .reconnectUrl

                            if (reconnectUrl != null) {
                                outcome = SessionOutcome.Reconnect(reconnectUrl)
                            }

                            return@webSocket
                        }

                        EventSubServerMessage.TYPE_REVOCATION -> {
                            logInfo<EventSubWebSocket> { "Subscription was revoked: ${message.payload}" }
                        }

                        else -> {
                            logDebug<EventSubWebSocket> { "Unknown message type ${message.metadata.messageType}" }
                        }
                    }
                }
            } finally {
                if (isAlive) {
                    updateConnectionStatus { it.copy(aliveConnections = it.aliveConnections - 1) }
                }
            }
        }

        return outcome
    }

    /**
     * Drops chat messages we've already emitted, and backfilled messages older than
     * the ones we've already received.
     */
    private fun shouldEmit(
        event: ChatEvent,
        state: CollectionState,
        isBackfill: Boolean,
    ): Boolean {
        if (event !is ChatEvent.Message) return true

        val lastMessageReceivedAt = state.lastMessageReceivedAt
        if (isBackfill && lastMessageReceivedAt != null && event.timestamp < lastMessageReceivedAt) {
            return false
        }

        val chatMessageId = event.chatMessageId
        if (chatMessageId != null) {
            if (!state.seenChatMessageIds.remember(chatMessageId, MAX_REMEMBERED_CHAT_MESSAGE_IDS)) {
                return false
            }

            state.lastMessageReceivedAt = event.timestamp
        }

        return true
    }

    private val ChatEvent.Message.chatMessageId: String?
        get() =
            when (this) {
                is ChatEvent.Message.ChatMessage -> id
                is ChatEvent.Message.HighlightedMessage -> userMessage.id
                is ChatEvent.Message.GigantifiedEmoteMessage -> userMessage.id
                else -> null
            }

    /**
     * Waits for the next parseable message, or returns null if none arrives before [timeout].
     */
    private suspend fun DefaultClientWebSocketSession.receiveMessage(timeout: Duration): EventSubServerMessage? =
        withTimeoutOrNull(timeout) {
            var message: EventSubServerMessage? = null
            while (message == null) {
                val frame = incoming.receive() as? Frame.Text ?: continue
                message =
                    try {
                        json.decodeFromString(EventSubServerMessage.serializer(), frame.readText())
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        logError<EventSubWebSocket>(e) { "Failed to parse message" }
                        null
                    }
            }
            message
        }

    /**
     * Creates all subscriptions for the given session.
     *
     * @return the plugins whose subscription was successfully created.
     */
    private suspend fun subscribe(
        sessionId: String,
        channelId: String,
        appUser: AppUser.LoggedIn,
    ): List<EventSubPlugin> =
        coroutineScope {
            plugins
                .map { plugin ->
                    async {
                        twitchClient
                            .createEventSubSubscription(
                                EventSubSubscriptionRequest(
                                    type = plugin.subscriptionType,
                                    version = plugin.subscriptionVersion,
                                    condition = plugin.getCondition(channelId, appUser),
                                    transport =
                                        EventSubSubscriptionRequest.Transport(
                                            sessionId = sessionId,
                                        ),
                                ),
                            ).onSuccess {
                                logDebug<EventSubWebSocket> { "Subscribed to ${plugin.subscriptionType}" }
                            }.onFailure { e ->
                                logError<EventSubWebSocket>(e) { "Failed to subscribe to ${plugin.subscriptionType}" }
                            }.map { plugin }
                            .getOrNull()
                    }
                }.awaitAll()
                .filterNotNull()
        }

    /**
     * Fetches the initial events of all [subscribedPlugins] concurrently, keeping the plugins' order.
     */
    private suspend fun getInitialEvents(
        subscribedPlugins: List<EventSubPlugin>,
        channelId: String,
        channelLogin: String,
        appUser: AppUser.LoggedIn,
    ): List<ChatEvent> =
        coroutineScope {
            subscribedPlugins
                .map { plugin ->
                    async {
                        try {
                            plugin.getInitialEvents(channelId, channelLogin, appUser)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            logError<EventSubWebSocket>(e) { "Failed to get initial events for ${plugin.subscriptionType}" }
                            emptyList()
                        }
                    }
                }.awaitAll()
                .flatten()
        }

    private fun parseNotification(message: EventSubServerMessage): List<ChatEvent> {
        val payload: EventSubNotificationPayload =
            try {
                json.decodeFromJsonElement(EventSubNotificationPayload.serializer(), message.payload)
            } catch (e: Exception) {
                logError<EventSubWebSocket>(e) { "Failed to parse notification payload" }
                return emptyList()
            }

        val event = payload.event ?: return emptyList()
        val plugin = plugins.firstOrNull { plugin -> plugin.subscriptionType == payload.subscription.type }

        if (plugin == null) {
            logDebug<EventSubWebSocket> { "No plugin for ${payload.subscription.type}" }
            return emptyList()
        }

        val timestamp: Instant =
            message.metadata.messageTimestamp
                ?.let { timestamp -> runCatching { Instant.parse(timestamp) }.getOrNull() }
                ?: clock.now()

        return try {
            plugin.parseEvent(event, timestamp)
        } catch (e: Exception) {
            logError<EventSubWebSocket>(e) { "Failed to parse ${payload.subscription.type} event" }
            emptyList()
        }
    }

    /**
     * Remembers [id], forgetting the oldest ids past [capacity].
     *
     * @return false if [id] was already known.
     */
    private fun ArrayDeque<String>.remember(
        id: String,
        capacity: Int,
    ): Boolean {
        if (id in this) return false
        if (size >= capacity) removeFirst()
        addLast(id)
        return true
    }
}
