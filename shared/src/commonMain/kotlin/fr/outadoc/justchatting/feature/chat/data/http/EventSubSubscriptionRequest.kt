package fr.outadoc.justchatting.feature.chat.data.http

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class EventSubSubscriptionRequest(
    @SerialName("type")
    val type: String,
    @SerialName("version")
    val version: String,
    @SerialName("condition")
    val condition: Map<String, String>,
    @SerialName("transport")
    val transport: Transport,
) {
    @Serializable
    data class Transport(
        @SerialName("method")
        val method: String = "websocket",
        @SerialName("session_id")
        val sessionId: String,
    )
}
