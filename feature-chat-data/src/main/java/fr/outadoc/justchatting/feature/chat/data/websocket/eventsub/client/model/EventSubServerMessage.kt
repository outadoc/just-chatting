package fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.client.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class EventSubServerMessage {

    @Serializable
    @SerialName("RESPONSE")
    data class Response(
        @SerialName("nonce")
        val nonce: String? = null,
        @SerialName("error")
        val error: String,
    ) : EventSubServerMessage()

    @Serializable
    @SerialName("MESSAGE")
    data class Message(
        @SerialName("nonce")
        val nonce: String? = null,
        @SerialName("data")
        val data: Data,
    ) : EventSubServerMessage() {

        @Serializable
        data class Data(
            @SerialName("topic")
            val topic: String,
            @SerialName("message")
            val message: String,
        )
    }

    @Serializable
    @SerialName("PONG")
    object Pong : EventSubServerMessage()

    @Serializable
    @SerialName("RECONNECT")
    object Reconnect : EventSubServerMessage()
}
