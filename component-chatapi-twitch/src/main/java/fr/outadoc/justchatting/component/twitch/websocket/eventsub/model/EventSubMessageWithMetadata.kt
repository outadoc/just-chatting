package fr.outadoc.justchatting.component.twitch.websocket.eventsub.model

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class EventSubMessageWithMetadata(
    @SerialName("metadata")
    val metadata: Metadata,
    @SerialName("payload")
    val payload: Payload,
) {
    @Serializable
    @JsonClassDiscriminator("message_type")
    @OptIn(ExperimentalSerializationApi::class)
    sealed class Metadata {

        abstract val messageId: String
        abstract val messageTimestamp: Instant

        @Serializable
        @SerialName("session_welcome")
        data class Welcome(
            @SerialName("message_id")
            override val messageId: String,
            @SerialName("message_timestamp")
            @Serializable(with = InstantIso8601Serializer::class)
            override val messageTimestamp: Instant,
        ) : Metadata()

        @Serializable
        @SerialName("session_keepalive")
        data class KeepAlive(
            @SerialName("message_id")
            override val messageId: String,
            @SerialName("message_timestamp")
            @Serializable(with = InstantIso8601Serializer::class)
            override val messageTimestamp: Instant,
        ) : Metadata()

        @Serializable
        @SerialName("session_reconnect")
        data class Reconnect(
            @SerialName("message_id")
            override val messageId: String,
            @SerialName("message_timestamp")
            @Serializable(with = InstantIso8601Serializer::class)
            override val messageTimestamp: Instant,
        ) : Metadata()

        @Serializable
        @SerialName("revocation")
        data class Revocation(
            @SerialName("message_id")
            override val messageId: String,
            @SerialName("message_timestamp")
            @Serializable(with = InstantIso8601Serializer::class)
            override val messageTimestamp: Instant,
            @SerialName("subscription_type")
            val subscriptionType: String,
            @SerialName("subscription_version")
            val subscriptionVersion: String,
        ) : Metadata()

        @Serializable
        @SerialName("notification")
        data class Notification(
            @SerialName("message_id")
            override val messageId: String,
            @SerialName("message_timestamp")
            @Serializable(with = InstantIso8601Serializer::class)
            override val messageTimestamp: Instant,
            @SerialName("subscription_type")
            val subscriptionType: String,
            @SerialName("subscription_version")
            val subscriptionVersion: String,
        ) : Metadata()
    }

    @Serializable
    data class Payload(
        @SerialName("subscription")
        val subscription: Subscription? = null,
        @SerialName("session")
        val session: Session? = null,
    ) {
        @Serializable
        data class Session(
            @SerialName("id")
            val id: String,
            @SerialName("status")
            val status: String,
            @SerialName("connected_at")
            val connectedAt: Instant,
            @SerialName("keepalive_timeout_seconds")
            val keepAliveTimeoutInSec: Int,
            @SerialName("reconnect_url")
            val reconnectUrl: String? = null,
        )

        @Serializable
        data class Subscription(
            @SerialName("id")
            val id: String,
            @SerialName("status")
            val status: String,
            @SerialName("type")
            val type: String,
            @SerialName("version")
            val version: String,
            @SerialName("created_at")
            @Serializable(with = InstantIso8601Serializer::class)
            val createdAt: Instant,
        )
    }
}
