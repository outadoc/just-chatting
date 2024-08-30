package fr.outadoc.justchatting.feature.chat.data.http

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SendMessageResponse(
    @SerialName("data")
    val data: MessageStatus,
) {
    @Serializable
    data class MessageStatus(
        @SerialName("message_id")
        val messageId: String,
        @SerialName("is_sent")
        val isSent: Boolean,
        @SerialName("drop_reason")
        val dropReason: DropReason? = null,
    )

    @Serializable
    data class DropReason(
        @SerialName("code")
        val code: Int,
        @SerialName("message")
        val message: String,
    )
}
