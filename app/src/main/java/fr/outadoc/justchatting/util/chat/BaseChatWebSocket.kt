package fr.outadoc.justchatting.util.chat

import android.content.Context
import fr.outadoc.justchatting.log.logDebug
import fr.outadoc.justchatting.log.logError
import fr.outadoc.justchatting.model.chat.ChatCommand
import fr.outadoc.justchatting.model.chat.Command
import fr.outadoc.justchatting.repository.AppPreferences
import fr.outadoc.justchatting.util.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

abstract class BaseChatWebSocket(
    private val applicationContext: Context,
    private val scope: CoroutineScope,
    private val clock: Clock,
    channelName: String
) {
    private companion object {
        const val SOCKET_ERROR_NORMAL_CLOSURE = 1_000
    }

    private var client: OkHttpClient = OkHttpClient()
    protected var socket: WebSocket? = null

    protected val hashChannelName: String = "#$channelName"

    private val _flow = MutableSharedFlow<ChatCommand>(
        replay = AppPreferences.Defaults.ChatLimitRange.last,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val flow: Flow<ChatCommand> = _flow

    protected fun connect(socketListener: WebSocketListener) {
        logDebug<BaseChatWebSocket> { "Connecting to Twitch IRC" }
        socket = client.newWebSocket(
            listener = socketListener,
            request = Request.Builder()
                .url("wss://irc-ws.chat.twitch.tv")
                .build()
        )
    }

    fun disconnect() {
        try {
            logDebug<BaseChatWebSocket> { "Disconnecting from $hashChannelName" }
            socket?.close(
                code = SOCKET_ERROR_NORMAL_CLOSURE,
                reason = null
            )
        } catch (e: IOException) {
            logError<BaseChatWebSocket>(e) { "Error while closing socketIn" }
            emit(
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

    protected fun emit(command: ChatCommand) {
        scope.launch {
            _flow.emit(command)
        }
    }

    protected fun emitAll(commands: List<ChatCommand>) {
        scope.launch {
            _flow.emitAll(commands.asFlow())
        }
    }

    protected fun attemptReconnect(listener: WebSocketListener) {
        scope.launch(Dispatchers.IO) {
            disconnect()

            while (isActive && !applicationContext.isNetworkAvailable) {
                delay(1.seconds)
            }

            delay(1.seconds)

            if (isActive) {
                connect(socketListener = listener)
            }
        }
    }
}
