package fr.outadoc.justchatting.feature.chat.data.websocket

import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.chat.data.ChatCommandHandlerFactory
import fr.outadoc.justchatting.feature.chat.data.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.data.model.ChatMessage
import fr.outadoc.justchatting.feature.chat.data.model.Command
import fr.outadoc.justchatting.feature.chat.data.model.HostModeState
import fr.outadoc.justchatting.feature.chat.data.model.PingCommand
import fr.outadoc.justchatting.feature.chat.data.model.PointReward
import fr.outadoc.justchatting.feature.chat.data.model.RoomStateDelta
import fr.outadoc.justchatting.feature.chat.data.model.UserState
import fr.outadoc.justchatting.feature.chat.data.parser.ChatMessageParser
import fr.outadoc.justchatting.feature.chat.data.recent.RecentMessagesRepository
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import kotlin.random.Random

/**
 * Live logged-out chat thread.
 *
 * Maintains a websocket connection to the IRC Twitch chat and notifies of all messages
 * and commands, except NOTICE and USERSTATE which are handled by [LoggedInChatWebSocket].
 */
class LiveChatWebSocket private constructor(
    networkStateObserver: NetworkStateObserver,
    private val scope: CoroutineScope,
    private val clock: Clock,
    private val parser: ChatMessageParser,
    private val recentMessagesRepository: RecentMessagesRepository,
    private val preferencesRepository: PreferenceRepository,
    private val channelLogin: String
) : BaseChatWebSocket(networkStateObserver, scope, channelLogin) {

    class Factory(
        private val networkStateObserver: NetworkStateObserver,
        private val clock: Clock,
        private val parser: ChatMessageParser,
        private val recentMessagesRepository: RecentMessagesRepository,
        private val preferencesRepository: PreferenceRepository
    ) : ChatCommandHandlerFactory {

        override fun create(
            scope: CoroutineScope,
            channelLogin: String,
            channelId: String
        ): LiveChatWebSocket {
            return LiveChatWebSocket(
                networkStateObserver = networkStateObserver,
                clock = clock,
                parser = parser,
                recentMessagesRepository = recentMessagesRepository,
                preferencesRepository = preferencesRepository,
                scope = scope,
                channelLogin = channelLogin
            )
        }
    }

    private val _connectionStatus: MutableStateFlow<ConnectionStatus> =
        MutableStateFlow(
            ConnectionStatus(
                isAlive = false,
                preventSendingMessages = false
            )
        )

    override val connectionStatus = _connectionStatus.asStateFlow()

    override fun start() {
        scope.launch {
            loadRecentMessages()
            connect(socketListener = LiveChatThreadListener())
        }
    }

    override fun send(message: CharSequence, inReplyToId: String?) {}

    private inner class LiveChatThreadListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            with(webSocket) {
                // random number between 1000 and 9999
                send("NICK justinfan${Random.nextInt(1000, 10_000)}")
                send("CAP REQ :twitch.tv/tags twitch.tv/commands")
                send("JOIN $hashChannelName")
            }

            logDebug<LiveChatWebSocket> { "Successfully connected to $hashChannelName" }

            emit(
                Command.Join(
                    channelLogin = channelLogin,
                    timestamp = clock.now()
                )
            )

            _connectionStatus.update { status -> status.copy(isAlive = true) }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            text.lineSequence()
                .filter { message -> message.isNotBlank() }
                .forEach(::notifyMessage)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            t.printStackTrace()
            _connectionStatus.update { status -> status.copy(isAlive = false) }

            emit(
                Command.Disconnect(
                    channelLogin = channelLogin,
                    timestamp = clock.now()
                )
            )

            attemptReconnect(listener = this@LiveChatThreadListener)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            _connectionStatus.update { status -> status.copy(isAlive = false) }
        }
    }

    private fun notifyMessage(message: String) {
        logDebug<LiveChatWebSocket> { message }

        when (val command = parser.parse(message)) {
            is ChatMessage,
            is Command.ClearChat,
            is Command.ClearMessage,
            is Command.UserNotice,
            is Command.Ban,
            is Command.Disconnect,
            is Command.Join,
            is Command.SendMessageError,
            is Command.Timeout,
            is HostModeState,
            is PointReward,
            is RoomStateDelta -> emit(command)

            is PingCommand -> sendPong()
            is Command.Notice,
            is UserState,
            null -> {
            }
        }
    }

    private suspend fun loadRecentMessages() {
        val prefs = preferencesRepository.currentPreferences.first()
        val recentMsgLimit = prefs.recentMsgLimit
        if (recentMsgLimit < 1) return

        try {
            emitAll(recentMessagesRepository.loadRecentMessages(channelLogin, recentMsgLimit))
        } catch (e: Exception) {
            logError<LiveChatWebSocket>(e) { "Failed to load recent messages for channel $channelLogin" }
        }
    }
}
