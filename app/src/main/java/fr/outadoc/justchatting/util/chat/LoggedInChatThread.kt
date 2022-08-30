package fr.outadoc.justchatting.util.chat

import android.util.Log
import fr.outadoc.justchatting.irc.ChatMessageParser
import fr.outadoc.justchatting.model.chat.Command
import fr.outadoc.justchatting.model.chat.LiveChatMessage
import fr.outadoc.justchatting.model.chat.PingCommand
import fr.outadoc.justchatting.model.chat.PubSubPointReward
import fr.outadoc.justchatting.model.chat.RoomState
import fr.outadoc.justchatting.model.chat.UserState
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException

/**
 * Logged in chat thread.
 *
 * Needed because user's own messages are only send when logged out. This thread handles
 * user-specific NOTICE and USERSTATE messages, and [LiveChatThread] handles the rest.
 *
 * Use this class to write messages to the chat.
 */
class LoggedInChatThread(
    scope: CoroutineScope,
    private val clock: Clock,
    private val userLogin: String?,
    private val userToken: String?,
    channelName: String,
    private val listener: OnMessageReceivedListener,
    private val parser: ChatMessageParser
) : BaseChatThread(scope, listener, clock, channelName) {

    fun start() {
        connect(socketListener = LiveChatThreadListener())
    }

    private inner class LiveChatThreadListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            with(webSocket) {
                send("PASS oauth:$userToken")
                send("NICK $userLogin")
                send("CAP REQ :twitch.tv/tags twitch.tv/commands")
                send("JOIN $hashChannelName")
            }

            Log.d(TAG, "Successfully logged in to $hashChannelName")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            text.lines()
                .filter { message -> message.isNotBlank() }
                .forEach(::notifyMessage)
        }

        private fun notifyMessage(message: String) {
            when (val command = parser.parse(message)) {
                is Command.Notice -> listener.onCommand(command)
                is UserState -> listener.onCommand(command)
                PingCommand -> sendPong()
                is LiveChatMessage,
                is PubSubPointReward,
                is Command.Ban,
                is Command.ClearChat,
                is Command.ClearMessage,
                is Command.Disconnect,
                is Command.Join,
                is Command.SendMessageError,
                is Command.SocketError,
                is Command.Timeout,
                is Command.UserNotice,
                is RoomState,
                null -> Unit
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            t.printStackTrace()
            attemptReconnect(listener = this@LiveChatThreadListener)
        }
    }

    fun send(message: CharSequence) {
        try {
            socket?.send("PRIVMSG $hashChannelName :$message")
            Log.d(TAG, "Sent message to $hashChannelName: $message")
        } catch (e: IOException) {
            Log.e(TAG, "Error sending message", e)
            listener.onCommand(
                Command.SendMessageError(
                    throwable = e,
                    timestamp = clock.now()
                )
            )
        }
    }
}
