package fr.outadoc.justchatting.component.twitch.websocket.pubsub.client.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class PubSubClientMessage {

    @Serializable
    @SerialName("LISTEN")
    data class Listen(
        @SerialName("nonce")
        val nonce: String? = null,
        @SerialName("data")
        val data: Data,
    ) : PubSubClientMessage() {

        @Serializable
        data class Data(
            @SerialName("topics")
            val topics: List<String>,
            @SerialName("auth_token")
            val authToken: String,
        )
    }

    @Serializable
    @SerialName("PING")
    object Ping : PubSubClientMessage()
}
