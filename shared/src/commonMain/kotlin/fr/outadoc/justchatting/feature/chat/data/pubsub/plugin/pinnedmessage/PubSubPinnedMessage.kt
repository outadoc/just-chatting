package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.pinnedmessage

import fr.outadoc.justchatting.utils.core.InstantUnixEpochSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
internal sealed class PubSubPinnedMessage {
    @Serializable
    @SerialName("pin-message")
    data class Pin(
        @SerialName("data")
        val data: Data,
    ) : PubSubPinnedMessage() {
        @Serializable
        data class Data(
            @SerialName("id")
            val pinId: String,
            @SerialName("pinned_by")
            val pinnedBy: User,
            @SerialName("message")
            val message: Message,
        )

        @Serializable
        data class User(
            @SerialName("id")
            val userId: String,
            @SerialName("display_name")
            val displayName: String,
        )

        @Serializable
        data class Message(
            @SerialName("id")
            val messageId: String,
            @SerialName("sender")
            val sender: User,
            @SerialName("content")
            val content: Content,
            @SerialName("starts_at")
            @Serializable(with = InstantUnixEpochSerializer::class)
            val startsAt: Instant,
            @SerialName("ends_at")
            @Serializable(with = InstantUnixEpochSerializer::class)
            val endsAt: Instant,
        ) {
            @Serializable
            data class Content(
                @SerialName("text")
                val text: String,
            )
        }
    }

    @Serializable
    @SerialName("update-message")
    data object Update : PubSubPinnedMessage()

    @Serializable
    @SerialName("unpin-message")
    data object Unpin : PubSubPinnedMessage()
}
