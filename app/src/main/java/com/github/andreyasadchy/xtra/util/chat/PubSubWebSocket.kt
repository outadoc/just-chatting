package com.github.andreyasadchy.xtra.util.chat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject

class PubSubWebSocket(
    channelId: String,
    private val coroutineScope: CoroutineScope,
    private val listener: OnMessageReceivedListener) {
    private var client: OkHttpClient? = null
    private var socket: WebSocket? = null
    private var pongReceived = false
    private val topics = listOf("community-points-channel-v1.$channelId")

    fun connect() {
        if (client == null) {
            client = OkHttpClient()
        }
        socket = client?.newWebSocket(Request.Builder().url("wss://pubsub-edge.twitch.tv").build(), PubSubListener())
    }

    fun disconnect() {
        socket?.close(1000, null)
        client?.dispatcher?.cancelAll()
    }

    private fun reconnect() {
        coroutineScope.launch {
            disconnect()
            delay(1000)
            connect()
        }
    }

    private fun listen() {
        val message = JSONObject().apply {
            put("type", "LISTEN")
            put("data", JSONObject().apply {
                put("topics", JSONArray().apply { topics.forEach { put(it) } })
            })
        }.toString()
        socket?.send(message)
    }

    private fun ping() {
        val ping = JSONObject().apply { put("type", "PING") }.toString()
        socket?.send(ping)
        checkPong()
    }

    private fun checkPong() {
        tickerFlow().onCompletion {
            if (pongReceived) {
                pongReceived = false
                delay(270000)
                ping()
            } else {
                reconnect()
            }
        }.launchIn(coroutineScope)
    }

    private fun tickerFlow() = flow {
        for (i in 10 downTo 0) {
            if (pongReceived) {
                emit(i downTo 0)
            } else {
                emit(i)
                delay(1000)
            }
        }
    }

    private inner class PubSubListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            listen()
            coroutineScope.launch {
                delay(270000)
                ping()
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val json = if (text.isNotBlank()) JSONObject(text) else null
            when (json?.optString("type")) {
                "MESSAGE" -> {
                    val data = json.optString("data").let { if (it.isNotBlank()) JSONObject(it) else null }
                    val topic = data?.optString("topic")
                    val messageType = data?.optString("message")?.let { if (it.isNotBlank()) JSONObject(it) else null }?.optString("type")
                    when {
                        (topic?.startsWith("community-points-channel") == true) && (messageType?.startsWith("reward-redeemed") == true) -> listener.onPointReward(text)
                    }
                }
                "PONG" -> pongReceived = true
                "RECONNECT" -> reconnect()
            }
        }
    }

    interface OnMessageReceivedListener {
        fun onPointReward(text: String)
    }
}
