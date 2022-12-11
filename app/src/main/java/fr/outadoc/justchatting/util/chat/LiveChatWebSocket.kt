package fr.outadoc.justchatting.util.chat

import fr.outadoc.justchatting.component.twitch.parser.ChatMessageParser
import fr.outadoc.justchatting.component.twitch.parser.model.ChatMessage
import fr.outadoc.justchatting.component.twitch.parser.model.Command
import fr.outadoc.justchatting.component.twitch.parser.model.HostModeState
import fr.outadoc.justchatting.component.twitch.parser.model.PingCommand
import fr.outadoc.justchatting.component.twitch.parser.model.PointReward
import fr.outadoc.justchatting.component.twitch.parser.model.RoomStateDelta
import fr.outadoc.justchatting.component.twitch.parser.model.UserState
import fr.outadoc.justchatting.repository.PreferenceRepository
import fr.outadoc.justchatting.repository.RecentMessagesRepository
import fr.outadoc.justchatting.util.NetworkStateObserver
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
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
) : BaseChatWebSocket(networkStateObserver, scope, clock, channelLogin) {

    class Factory(
        private val networkStateObserver: NetworkStateObserver,
        private val clock: Clock,
        private val parser: ChatMessageParser,
        private val recentMessagesRepository: RecentMessagesRepository,
        private val preferencesRepository: PreferenceRepository
    ) {
        fun create(scope: CoroutineScope, channelLogin: String): LiveChatWebSocket {
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

    fun start() {
        scope.launch {
            loadRecentMessages()
            connect(socketListener = LiveChatThreadListener())
        }
    }

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
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            text.lineSequence()
                .filter { message -> message.isNotBlank() }
                .forEach(::notifyMessage)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            t.printStackTrace()

            emit(
                Command.Disconnect(
                    channelLogin = channelLogin,
                    throwable = t,
                    timestamp = clock.now()
                )
            )

            attemptReconnect(listener = this@LiveChatThreadListener)
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
            is Command.SocketError,
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
            recentMessagesRepository.loadRecentMessages(channelLogin, recentMsgLimit)
                .body()
                ?.messages
                ?.let { commands ->
                    emitAll(commands)
                }
        } catch (e: Exception) {
            logError<LiveChatWebSocket>(e) { "Failed to load recent messages for channel $channelLogin" }
        }
    }
}
