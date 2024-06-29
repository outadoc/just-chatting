package fr.outadoc.justchatting.feature.chat.data.irc.recent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RecentMessagesResponse(
    @SerialName("messages")
    val messages: List<String>,
)
