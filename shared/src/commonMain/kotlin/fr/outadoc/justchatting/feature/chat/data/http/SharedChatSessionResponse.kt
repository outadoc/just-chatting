package fr.outadoc.justchatting.feature.chat.data.http

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SharedChatSessionResponse(
    @SerialName("data")
    val data: List<Session>,
) {
    @Serializable
    data class Session(
        @SerialName("host_broadcaster_id")
        val hostBroadcasterId: String,
        @SerialName("participants")
        val participants: List<Participant>,
    )

    @Serializable
    data class Participant(
        @SerialName("broadcaster_id")
        val broadcasterId: String,
    )
}
