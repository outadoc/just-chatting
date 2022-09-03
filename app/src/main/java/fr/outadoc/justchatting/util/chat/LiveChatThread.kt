package fr.outadoc.justchatting.util.chat

import android.util.Log
import fr.outadoc.justchatting.irc.ChatMessageParser
import fr.outadoc.justchatting.model.chat.ChatMessage
import fr.outadoc.justchatting.model.chat.Command
import fr.outadoc.justchatting.model.chat.PingCommand
import fr.outadoc.justchatting.model.chat.RoomState
import fr.outadoc.justchatting.model.chat.UserState
import kotlinx.coroutines.CoroutineScope
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
 * and commands, except NOTICE and USERSTATE which are handled by [LoggedInChatThread].
 */
class LiveChatThread(
    scope: CoroutineScope,
    private val clock: Clock,
    private val channelName: String,
    private val listener: OnCommandReceivedListener,
    private val parser: ChatMessageParser
) : BaseChatThread(scope, listener, clock, channelName) {

    fun start() {
        connect(socketListener = LiveChatThreadListener())
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

            listener.onCommand(
                Command.Join(
                    channelName = channelName,
                    timestamp = clock.now()
                )
            )
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            text.lines()
                .filter { message -> message.isNotBlank() }
                .forEach(::notifyMessage)
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
                is RoomState -> {
                    listener.onCommand(command)
                }
                PingCommand -> {
                    sendPong()
                }
                is Command.Notice,
                is UserState,
                null -> Unit
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            t.printStackTrace()

            if (t is IOException && !t.isSocketError) {
                listener.onCommand(
                    Command.Disconnect(
                        channelName = channelName,
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
}
