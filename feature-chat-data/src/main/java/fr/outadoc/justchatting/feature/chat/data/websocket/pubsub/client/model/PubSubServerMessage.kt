package fr.outadoc.justchatting.feature.chat.data.websocket.pubsub.client.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class PubSubServerMessage {

    @Serializable
    @SerialName("RESPONSE")
    data class Response(
        @SerialName("nonce")
        val nonce: String? = null,
        @SerialName("error")
        val error: String,
    ) : PubSubServerMessage()

    @Serializable
    @SerialName("MESSAGE")
    data class Message(
        @SerialName("nonce")
        val nonce: String? = null,
        @SerialName("data")
        val data: Data,
    ) : PubSubServerMessage() {

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
    object Pong : PubSubServerMessage()

    @Serializable
    @SerialName("RECONNECT")
    object Reconnect : PubSubServerMessage()
}
