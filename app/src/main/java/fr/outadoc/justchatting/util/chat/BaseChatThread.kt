package fr.outadoc.justchatting.util.chat

import android.util.Log
import fr.outadoc.justchatting.MainApplication
import fr.outadoc.justchatting.model.chat.Command
import fr.outadoc.justchatting.util.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

const val TAG = "ChatThread"

abstract class BaseChatThread(
    private val scope: CoroutineScope,
    private val listener: OnCommandReceivedListener,
    private val clock: Clock,
    channelName: String
) {
    private val appContext = MainApplication.INSTANCE.applicationContext

    private var client: OkHttpClient = OkHttpClient()
    protected var socket: WebSocket? = null

    protected val hashChannelName: String = "#$channelName"

    protected fun connect(socketListener: WebSocketListener) {
        Log.d(TAG, "Connecting to Twitch IRC")
        socket = client.newWebSocket(
            listener = socketListener,
            request = Request.Builder()
                .url("wss://irc-ws.chat.twitch.tv")
                .build()
        )
    }

    fun disconnect() {
        try {
            Log.d(TAG, "Disconnecting from $hashChannelName")
            socket?.close(code = 1000, reason = null)
            client.dispatcher.cancelAll()
        } catch (e: IOException) {
            Log.e(TAG, "Error while closing socketIn", e)
            listener.onCommand(
                Command.SocketError(
                    throwable = e,
                    timestamp = clock.now()
                )
            )
        }
    }

    @Throws(IOException::class)
    protected fun sendPong() {
        socket?.send("PONG :tmi.twitch.tv")
    }

    protected fun attemptReconnect(listener: WebSocketListener) {
        scope.launch(Dispatchers.IO) {
            disconnect()

            while (isActive && !appContext.isNetworkAvailable) {
                delay(1.seconds)
            }

            delay(1.seconds)

            if (isActive) {
                connect(socketListener = listener)
            }
        }
    }
}
