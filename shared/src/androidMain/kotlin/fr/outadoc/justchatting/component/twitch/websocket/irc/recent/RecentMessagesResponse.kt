package fr.outadoc.justchatting.component.twitch.websocket.irc.recent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecentMessagesResponse(
    @SerialName("messages")
    val messages: List<String>,
)
