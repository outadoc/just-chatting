package fr.outadoc.justchatting.component.chat.data.websocket

import fr.outadoc.justchatting.component.chat.data.model.ChatCommand
import fr.outadoc.justchatting.component.chat.data.model.Command
import fr.outadoc.justchatting.component.preferences.AppPreferences
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
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
    private val networkStateObserver: NetworkStateObserver,
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

    private var isNetworkAvailable: Boolean = false

    init {
        scope.launch {
            networkStateObserver.state.collectLatest { state ->
                isNetworkAvailable = state is NetworkStateObserver.NetworkState.Available
            }
        }
    }

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

            while (isActive && !isNetworkAvailable) {
                delay(1.seconds)
            }

            delay(1.seconds)

            if (isActive) {
                connect(socketListener = listener)
            }
        }
    }
}
