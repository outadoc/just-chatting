package fr.outadoc.justchatting.util.chat

import android.content.Context
import android.util.Log
import fr.outadoc.justchatting.irc.ChatMessageParser
import fr.outadoc.justchatting.model.chat.Command
import fr.outadoc.justchatting.model.chat.ChatMessage
import fr.outadoc.justchatting.model.chat.PingCommand
import fr.outadoc.justchatting.model.chat.PointReward
import fr.outadoc.justchatting.model.chat.RoomState
import fr.outadoc.justchatting.model.chat.UserState
import fr.outadoc.justchatting.repository.ChatPreferencesRepository
import fr.outadoc.justchatting.repository.RecentMessagesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException
import kotlin.random.Random

/**
 * Live logged-out chat thread.
 *
 * Maintains a websocket connection to the IRC Twitch chat and notifies of all messages
 * and commands, except NOTICE and USERSTATE which are handled by [LoggedInChatWebSocket].
 */
class LiveChatWebSocket private constructor(
    applicationContext: Context,
    private val scope: CoroutineScope,
    private val clock: Clock,
    private val parser: ChatMessageParser,
    private val recentMessagesRepository: RecentMessagesRepository,
    private val chatPreferencesRepository: ChatPreferencesRepository,
    private val channelLogin: String
) : BaseChatWebSocket(applicationContext, scope, clock, channelLogin) {

    class Factory(
        private val applicationContext: Context,
        private val clock: Clock,
        private val parser: ChatMessageParser,
        private val recentMessagesRepository: RecentMessagesRepository,
        private val chatPreferencesRepository: ChatPreferencesRepository
    ) {
        fun create(scope: CoroutineScope, channelLogin: String): LiveChatWebSocket {
            return LiveChatWebSocket(
                applicationContext = applicationContext,
                clock = clock,
                parser = parser,
                recentMessagesRepository = recentMessagesRepository,
                chatPreferencesRepository = chatPreferencesRepository,
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

            Log.d(TAG, "Successfully connected to $hashChannelName")

            emit(
                Command.Join(
                    channelLogin = channelLogin,
                    timestamp = clock.now()
                )
            )
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            text.lines()
                .filter { message -> message.isNotBlank() }
                .forEach(::notifyMessage)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            t.printStackTrace()

            if (t is IOException && !t.isSocketError) {
                emit(
                    Command.Disconnect(
                        channelLogin = channelLogin,
                        throwable = t,
                        timestamp = clock.now()
                    )
                )
            }

            attemptReconnect(listener = this@LiveChatThreadListener)
        }

        private val Throwable.isSocketError: Boolean
            get() = when (message) {
                "Socket closed",
                "socket is closed",
                "Connection reset",
                "recvfrom failed: ECONNRESET (Connection reset by peer)" -> true
                else -> false
            }
    }

    private fun notifyMessage(message: String) {
        Log.d(TAG, message)

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
            is PointReward,
            is RoomState -> emit(command)
            PingCommand -> sendPong()
            is Command.Notice,
            is UserState,
            null -> {
            }
        }
    }

    private suspend fun loadRecentMessages() {
        val recentMsgLimit = chatPreferencesRepository.recentMsgLimit.first()
        if (recentMsgLimit < 1) return

        try {
            recentMessagesRepository.loadRecentMessages(channelLogin, recentMsgLimit)
                .body()
                ?.messages
                ?.let { commands ->
                    emitAll(commands)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load recent messages for channel $channelLogin", e)
        }
    }
}
