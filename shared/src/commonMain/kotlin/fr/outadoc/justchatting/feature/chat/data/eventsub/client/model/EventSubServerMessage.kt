package fr.outadoc.justchatting.feature.chat.data.eventsub.client.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * A message received on the EventSub WebSocket.
 *
 * The shape of [payload] depends on [Metadata.messageType].
 *
 * @see <a href="https://dev.twitch.tv/docs/eventsub/websocket-reference/">WebSocket reference</a>
 */
@Serializable
internal data class EventSubServerMessage(
    @SerialName("metadata")
    val metadata: Metadata,
    @SerialName("payload")
    val payload: JsonObject,
) {
    @Serializable
    data class Metadata(
        @SerialName("message_id")
        val messageId: String,
        @SerialName("message_type")
        val messageType: String,
    )

    companion object {
        const val TYPE_WELCOME = "session_welcome"
        const val TYPE_KEEPALIVE = "session_keepalive"
        const val TYPE_NOTIFICATION = "notification"
        const val TYPE_RECONNECT = "session_reconnect"
        const val TYPE_REVOCATION = "revocation"
    }
}

/**
 * Payload of `session_welcome` and `session_reconnect` messages.
 */
@Serializable
internal data class EventSubSessionPayload(
    @SerialName("session")
    val session: Session,
) {
    @Serializable
    data class Session(
        @SerialName("id")
        val id: String,
        @SerialName("keepalive_timeout_seconds")
        val keepaliveTimeoutSeconds: Int? = null,
        @SerialName("reconnect_url")
        val reconnectUrl: String? = null,
    )
}

/**
 * Payload of `notification` and `revocation` messages.
 */
@Serializable
internal data class EventSubNotificationPayload(
    @SerialName("subscription")
    val subscription: Subscription,
    @SerialName("event")
    val event: JsonObject? = null,
) {
    @Serializable
    data class Subscription(
        @SerialName("type")
        val type: String,
        @SerialName("status")
        val status: String? = null,
    )
}
