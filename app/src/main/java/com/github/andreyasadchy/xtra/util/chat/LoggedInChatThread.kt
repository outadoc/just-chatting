package com.github.andreyasadchy.xtra.util.chat

import android.util.Log
import com.github.andreyasadchy.xtra.model.chat.Command
import com.github.andreyasadchy.xtra.model.chat.PingCommand
import com.github.andreyasadchy.xtra.model.chat.UserState
import kotlinx.coroutines.CoroutineScope
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException

class LoggedInChatThread(
    scope: CoroutineScope,
    private val userLogin: String?,
    private val userToken: String?,
    channelName: String,
    private val listener: OnMessageReceivedListener,
    private val parser: ChatMessageParser
) : BaseChatThread(scope, listener, channelName) {

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
                else -> {}
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
                    message = e.toString(),
                    fullMsg = e.stackTraceToString()
                )
            )

        }
    }
}
