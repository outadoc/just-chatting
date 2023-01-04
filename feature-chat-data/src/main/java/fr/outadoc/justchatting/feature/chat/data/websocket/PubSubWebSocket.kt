package fr.outadoc.justchatting.feature.chat.data.websocket

import fr.outadoc.justchatting.component.preferences.data.AppPreferences
import fr.outadoc.justchatting.feature.chat.data.ChatCommandHandler
import fr.outadoc.justchatting.feature.chat.data.ChatCommandHandlerFactory
import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import kotlin.time.Duration.Companion.seconds

class PubSubWebSocket(
    private val networkStateObserver: NetworkStateObserver,
    private val pubSubRewardParser: PubSubRewardParser,
    private val scope: CoroutineScope,
    channelId: String
) : ChatCommandHandler {

    class Factory(
        private val networkStateObserver: NetworkStateObserver,
        private val pubSubRewardParser: PubSubRewardParser
    ) : ChatCommandHandlerFactory {

        override fun create(
            scope: CoroutineScope,
            channelLogin: String,
            channelId: String
        ): PubSubWebSocket {
            return PubSubWebSocket(
                networkStateObserver = networkStateObserver,
                pubSubRewardParser = pubSubRewardParser,
                scope = scope,
                channelId = channelId
            )
        }
    }

    private var client: OkHttpClient = OkHttpClient()
    private var socket: WebSocket? = null

    private val topics = listOf("community-points-channel-v1.$channelId")

    private val _flow = MutableSharedFlow<ChatCommand>(
        replay = AppPreferences.Defaults.ChatLimitRange.last,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val commandFlow: Flow<ChatCommand> = _flow

    private var pongReceived = false
    private var isNetworkAvailable: Boolean = false

    init {
        scope.launch {
            networkStateObserver.state.collectLatest { state ->
                isNetworkAvailable = state is NetworkStateObserver.NetworkState.Available
            }
        }
    }

    override fun start() {
        connect(listener = PubSubListener())
    }

    private fun connect(listener: WebSocketListener) {
        socket = client.newWebSocket(
            request = Request.Builder().url("wss://pubsub-edge.twitch.tv").build(),
            listener = listener
        )
    }

    override fun disconnect() {
        socket?.close(1000, null)
        client.dispatcher.cancelAll()
    }

    private fun attemptReconnect(listener: WebSocketListener) {
        scope.launch(Dispatchers.IO) {
            disconnect()

            while (isActive && !isNetworkAvailable) {
                delay(1.seconds)
            }

            delay(1.seconds)

            if (isActive) {
                connect(listener = listener)
            }
        }
    }

    private fun listen() {
        val message = JSONObject().apply {
            put("type", "LISTEN")
            put(
                "data",
                JSONObject().apply {
                    put("topics", JSONArray().apply { topics.forEach { put(it) } })
                }
            )
        }.toString()
        socket?.send(message)
    }

    private fun ping(listener: WebSocketListener) {
        val ping = JSONObject().apply { put("type", "PING") }.toString()
        socket?.send(ping)
        checkPong(listener)
    }

    private fun checkPong(listener: WebSocketListener) {
        tickerFlow()
            .onCompletion {
                if (pongReceived) {
                    pongReceived = false
                    delay(270.seconds)
                    ping(listener)
                } else {
                    attemptReconnect(listener)
                }
            }
            .launchIn(scope)
    }

    private fun tickerFlow() = flow {
        for (i in 10 downTo 0) {
            if (pongReceived) {
                emit(i downTo 0)
            } else {
                emit(i)
                delay(1.seconds)
            }
        }
    }

    override fun send(message: CharSequence, inReplyToId: String?) {}

    private inner class PubSubListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            listen()
            ping(this)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val json = if (text.isNotBlank()) JSONObject(text) else null
            when (json?.optString("type")) {
                "MESSAGE" -> {
                    val data: JSONObject? =
                        json.optString("data").let {
                            if (it.isNotBlank()) JSONObject(it)
                            else null
                        }

                    val topic = data?.optString("topic")
                    val messageType = data?.optString("message")
                        ?.let { if (it.isNotBlank()) JSONObject(it) else null }
                        ?.optString("type")

                    when {
                        topic?.startsWith("community-points-channel") == true &&
                                messageType?.startsWith("reward-redeemed") == true -> {
                            scope.launch {
                                _flow.emit(
                                    pubSubRewardParser.parse(text)
                                )
                            }
                        }
                    }
                }

                "PONG" -> pongReceived = true
                "RECONNECT" -> attemptReconnect(this)
            }
        }
    }
}
