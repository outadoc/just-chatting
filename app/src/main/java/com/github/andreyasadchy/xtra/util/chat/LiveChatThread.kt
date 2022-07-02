package com.github.andreyasadchy.xtra.util.chat

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException
import kotlin.random.Random

class LiveChatThread(
    scope: CoroutineScope,
    private val loggedIn: Boolean,
    private val channelName: String,
    private val listener: OnMessageReceivedListener
) : BaseChatThread(scope, listener, channelName) {

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
                message = channelName,
                duration = null,
                type = "join",
                fullMsg = null
            )
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            text.lines().forEach(::notifyMessage)
        }

        private fun notifyMessage(message: String) = with(message) {
            when {
                contains("PRIVMSG") -> listener.onMessage(this, false)
                contains("USERNOTICE") -> listener.onMessage(this, true)
                contains("CLEARMSG") -> listener.onClearMessage(this)
                contains("CLEARCHAT") -> listener.onClearChat(this)
                contains("NOTICE") -> if (!loggedIn) listener.onNotice(this)
                contains("ROOMSTATE") -> listener.onRoomState(this)
                startsWith("PING") -> sendPong()
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            t.printStackTrace()

            if (t is IOException && !t.isSocketError) {
                listener.onCommand(
                    message = channelName,
                    duration = t.toString(),
                    type = "disconnect",
                    fullMsg = t.stackTraceToString()
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
