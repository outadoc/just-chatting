package fr.outadoc.justchatting.component.twitch.http.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Subscription(
    @SerialName("type")
    val type: String,
    @SerialName("version")
    val version: String = "1",
    @SerialName("condition")
    val condition: Condition,
    @SerialName("transport")
    val transport: Transport,
) {
    @Serializable
    data class Condition(
        @SerialName("broadcaster_user_id")
        val broadcasterUserId: String,
    )

    @Serializable
    data class Transport(
        @SerialName("method")
        val method: String = "websocket",
        @SerialName("session_id")
        val sessionId: String,
    )
}
