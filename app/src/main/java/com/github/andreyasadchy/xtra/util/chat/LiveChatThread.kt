package com.github.andreyasadchy.xtra.util.chat

import android.util.Log
import com.github.andreyasadchy.xtra.feature.irc.ChatMessageParser
import com.github.andreyasadchy.xtra.model.chat.ChatMessage
import com.github.andreyasadchy.xtra.model.chat.Command
import com.github.andreyasadchy.xtra.model.chat.PingCommand
import com.github.andreyasadchy.xtra.model.chat.RoomState
import com.github.andreyasadchy.xtra.model.chat.UserState
import kotlinx.coroutines.CoroutineScope
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException
import kotlin.random.Random

class LiveChatThread(
    scope: CoroutineScope,
    private val channelName: String,
    private val listener: OnMessageReceivedListener,
    private val parser: ChatMessageParser
) : BaseChatThread(scope, listener, channelName) {

    fun start(isLoggedIn: Boolean) {
        connect(socketListener = LiveChatThreadListener(isLoggedIn))
    }

    private inner class LiveChatThreadListener(
        private val isLoggedIn: Boolean
    ) : WebSocketListener() {

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
                    message = channelName,
                    duration = null
                )
            )
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            text.lines()
                .filter { message -> message.isNotBlank() }
                .forEach(::notifyMessage)
        }

        private fun notifyMessage(message: String) {
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
                is Command.Notice -> {
                    if (!isLoggedIn) listener.onCommand(command)
                }
                is UserState,
                null -> Unit
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            t.printStackTrace()

            if (t is IOException && !t.isSocketError) {
                listener.onCommand(
                    Command.Disconnect(
                        message = channelName,
                        throwable = t
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
