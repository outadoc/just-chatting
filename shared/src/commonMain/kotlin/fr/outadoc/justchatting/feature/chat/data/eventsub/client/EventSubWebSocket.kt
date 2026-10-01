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
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Receives channel events from Twitch EventSub over a WebSocket.
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

        const val MAX_REMEMBERED_MESSAGE_IDS = 100
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

    private val plugins: List<EventSubPlugin> = eventSubPluginsProvider.get()

    /**
     * EventSub only carries auxiliary events, so it never reports the chat as disconnected.
     */
    override val connectionStatus: Flow<ConnectionStatus> = flowOf(ConnectionStatus(isAlive = true))

    override fun getEventFlow(
        channelId: String,
        channelLogin: String,
        appUser: AppUser.LoggedIn,
    ): Flow<ChatEvent> =
        channelFlow {
            networkStateObserver.state.collectLatest { netState ->
                if (netState !is NetworkStateObserver.NetworkState.Available) {
                    logDebug<EventSubWebSocket> { "Network is out, waiting" }
                    return@collectLatest
                }

                logDebug<EventSubWebSocket> { "Network is available, connecting" }

                // Twitch may resend messages; remember the ones we've seen across reconnects
                val seenMessageIds = ArrayDeque<String>()

                var url = ENDPOINT
                var shouldSubscribe = true

                while (currentCoroutineContext().isActive) {
                    val outcome =
                        try {
                            listen(
                                url = url,
                                shouldSubscribe = shouldSubscribe,
                                channelId = channelId,
                                appUser = appUser,
                                seenMessageIds = seenMessageIds,
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
                            shouldSubscribe = false
                        }

                        SessionOutcome.Disconnected -> {
                            url = ENDPOINT
                            shouldSubscribe = true
                            delayWithJitter(3.seconds, maxJitter = 5.seconds)
                        }

                        SessionOutcome.NoSubscriptions -> {
                            logInfo<EventSubWebSocket> { "No subscriptions were created, giving up" }
                            awaitCancellation()
                        }
                    }
                }
            }
        }.flowOn(dispatchersProvider.io)

    private suspend fun ProducerScope<ChatEvent>.listen(
        url: String,
        shouldSubscribe: Boolean,
        channelId: String,
        appUser: AppUser.LoggedIn,
        seenMessageIds: ArrayDeque<String>,
    ): SessionOutcome {
        var outcome: SessionOutcome = SessionOutcome.Disconnected

        httpClient.webSocket(url) {
            var keepaliveTimeout: Duration = DEFAULT_KEEPALIVE_TIMEOUT

            while (isActive) {
                val message: EventSubServerMessage =
                    receiveMessage(timeout = keepaliveTimeout + KEEPALIVE_GRACE_PERIOD)
                        ?: run {
                            logInfo<EventSubWebSocket> { "No message received before keepalive timeout" }
                            return@webSocket
                        }

                if (!seenMessageIds.remember(message.metadata.messageId)) {
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

                        if (shouldSubscribe) {
                            val subscribedCount =
                                subscribe(
                                    sessionId = session.id,
                                    channelId = channelId,
                                    appUser = appUser,
                                )

                            if (subscribedCount == 0) {
                                outcome = SessionOutcome.NoSubscriptions
                                return@webSocket
                            }
                        }
                    }

                    EventSubServerMessage.TYPE_KEEPALIVE -> {}

                    EventSubServerMessage.TYPE_NOTIFICATION -> {
                        handleNotification(message) { event ->
                            this@listen.send(event)
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
        }

        return outcome
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
     * @return the number of subscriptions that were successfully created.
     */
    private suspend fun subscribe(
        sessionId: String,
        channelId: String,
        appUser: AppUser.LoggedIn,
    ): Int =
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
                            }.isSuccess
                    }
                }.awaitAll()
                .count { isSuccess -> isSuccess }
        }

    private suspend fun handleNotification(
        message: EventSubServerMessage,
        emit: suspend (ChatEvent) -> Unit,
    ) {
        val payload: EventSubNotificationPayload =
            try {
                json.decodeFromJsonElement(EventSubNotificationPayload.serializer(), message.payload)
            } catch (e: Exception) {
                logError<EventSubWebSocket>(e) { "Failed to parse notification payload" }
                return
            }

        val event = payload.event ?: return
        val plugin = plugins.firstOrNull { plugin -> plugin.subscriptionType == payload.subscription.type }

        if (plugin == null) {
            logDebug<EventSubWebSocket> { "No plugin for ${payload.subscription.type}" }
            return
        }

        val events: List<ChatEvent> =
            try {
                plugin.parseEvent(event)
            } catch (e: Exception) {
                logError<EventSubWebSocket>(e) { "Failed to parse ${payload.subscription.type} event" }
                return
            }

        events.forEach { chatEvent -> emit(chatEvent) }
    }

    /**
     * Remembers [id], forgetting the oldest ids when full.
     *
     * @return false if [id] was already known.
     */
    private fun ArrayDeque<String>.remember(id: String): Boolean {
        if (id in this) return false
        if (size >= MAX_REMEMBERED_MESSAGE_IDS) removeFirst()
        addLast(id)
        return true
    }
}
