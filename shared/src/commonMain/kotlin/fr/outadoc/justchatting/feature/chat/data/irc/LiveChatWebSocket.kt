package fr.outadoc.justchatting.feature.chat.data.irc

import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesRepository
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import fr.outadoc.justchatting.utils.logging.logError
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Live logged-out chat thread.
 *
 * Maintains a websocket connection to the IRC Twitch chat and notifies of all messages
 * and commands, except NOTICE and USERSTATE which are handled by [LoggedInChatWebSocket].
 */
internal class LiveChatWebSocket(
    networkStateObserver: NetworkStateObserver,
    private val clock: Clock,
    parser: TwitchIrcCommandParser,
    httpClient: HttpClient,
    private val recentMessagesRepository: RecentMessagesRepository,
    private val preferencesRepository: PreferenceRepository,
    dispatchersProvider: DispatchersProvider,
    endpoint: String = DEFAULT_ENDPOINT,
    messageTimeout: Duration = DEFAULT_MESSAGE_TIMEOUT,
) : BaseChatWebSocket(
        networkStateObserver = networkStateObserver,
        parser = parser,
        httpClient = httpClient,
        dispatchersProvider = dispatchersProvider,
        endpoint = endpoint,
        messageTimeout = messageTimeout,
    ) {
    companion object {
        private const val SEEN_MESSAGE_IDS_LIMIT = AppPreferences.Defaults.RecentChatLimit * 2
    }

    override val logTag: String = "LiveChatWebSocket"

    /**
     * State shared between reconnections of a single collection, used to avoid
     * replaying messages that were already emitted.
     */
    private class SessionState {
        var lastMessageReceivedAt: Instant? = null

        private val seenMessageIds = ArrayDeque<String>()

        // Returns false if this message was already emitted during this session.
        fun markSeen(event: ChatEvent.Message): Boolean {
            val id = (event as? ChatEvent.Message.ChatMessage)?.id ?: return true
            if (id in seenMessageIds) return false
            seenMessageIds += id
            while (seenMessageIds.size > SEEN_MESSAGE_IDS_LIMIT) {
                seenMessageIds.removeFirst()
            }
            return true
        }
    }

    override fun getEventFlow(
        channelId: String,
        channelLogin: String,
        appUser: AppUser.LoggedIn,
    ): Flow<ChatEvent> =
        chatEventFlow {
            val state = SessionState()
            Session(
                onConnected = {
                    // random number between 1000 and 9999
                    sendCommand("NICK justinfan${Random.nextInt(1000, 10_000)}")
                    sendCommand("CAP REQ :twitch.tv/tags twitch.tv/commands")
                    sendCommand("JOIN #$channelLogin")

                    emit(
                        ChatEvent.Message.Join(
                            timestamp = clock.now(),
                            channelLogin = channelLogin,
                        ),
                    )

                    // Join before backfilling: messages that arrive while recent messages
                    // are being fetched wait in the socket's buffer instead of being missed.
                    loadRecentMessages(channelLogin, state)
                },
                onCommandReceived = { command -> handleCommand(command, state) },
            )
        }

    private suspend fun Connection.handleCommand(
        command: ChatEvent,
        state: SessionState,
    ) {
        when (command) {
            is ChatEvent.Command.UserState,
            is ChatEvent.Message.Notice,
            -> {
                // Handled by LoggedInChatWebSocket
            }

            is ChatEvent.Message -> {
                if (state.markSeen(command)) {
                    emit(command)
                    state.lastMessageReceivedAt = command.timestamp
                }
            }

            is ChatEvent.Command.RoomStateDelta,
            is ChatEvent.Command.ClearChat,
            is ChatEvent.Command.ClearMessage,
            -> {
                emit(command)
            }

            else -> {}
        }
    }

    private suspend fun Connection.loadRecentMessages(
        channelLogin: String,
        state: SessionState,
    ) {
        val prefs = preferencesRepository.currentPreferences.first()
        if (!prefs.enableRecentMessages) return

        recentMessagesRepository
            .loadRecentMessages(
                channelLogin = channelLogin,
                limit = AppPreferences.Defaults.RecentChatLimit,
            ).map { messages ->
                messages
                    .filterIsInstance<ChatEvent.Message>()
                    .filter { event ->
                        event.timestamp >= (state.lastMessageReceivedAt ?: Instant.DISTANT_PAST)
                    }
            }.fold(
                onSuccess = { events ->
                    events.forEach { event ->
                        if (state.markSeen(event)) {
                            emit(event)
                            state.lastMessageReceivedAt = event.timestamp
                        }
                    }
                },
                onFailure = { e ->
                    logError<LiveChatWebSocket>(e) { "Failed to load recent messages for channel $channelLogin" }
                },
            )
    }
}
