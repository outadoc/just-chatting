package fr.outadoc.justchatting.feature.chat.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecentMessagesResponse(
    @SerialName("messages")
    val messages: List<String>,
)
