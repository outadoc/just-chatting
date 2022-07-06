package com.github.andreyasadchy.xtra.util.chat

import com.github.andreyasadchy.xtra.XtraApp
import com.tonyodev.fetch2core.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val scope: CoroutineScope,
    channelId: String,
    private val listener: OnMessageReceivedListener
) {
    private val appContext = XtraApp.INSTANCE.applicationContext

    private var client: OkHttpClient = OkHttpClient()
    private var socket: WebSocket? = null

    private var pongReceived = false

    private val topics = listOf("community-points-channel-v1.$channelId")

    fun start() {
        connect(listener = PubSubListener())
    }

    private fun connect(listener: WebSocketListener) {
        socket = client.newWebSocket(
            request = Request.Builder().url("wss://pubsub-edge.twitch.tv").build(),
            listener = listener
        )
    }

    fun disconnect() {
        socket?.close(1000, null)
        client.dispatcher.cancelAll()
    }

    private fun attemptReconnect(listener: WebSocketListener) {
        scope.launch(Dispatchers.IO) {
            disconnect()

            while (isActive && !appContext.isNetworkAvailable()) {
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
                            listener.onPointReward(text)
                        }
                    }
                }
                "PONG" -> pongReceived = true
                "RECONNECT" -> attemptReconnect(this)
            }
        }
    }

    interface OnMessageReceivedListener {
        fun onPointReward(text: String)
    }
}
