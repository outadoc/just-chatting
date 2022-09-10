package fr.outadoc.justchatting.ui.view.chat.model

import fr.outadoc.justchatting.model.chat.Badge
import fr.outadoc.justchatting.model.chat.ChatMessage
import fr.outadoc.justchatting.model.chat.TwitchChatEmote
import kotlinx.datetime.Instant

sealed class ChatEntry {

    abstract val data: Data?
    abstract val timestamp: Instant?

    data class Data(
        val userId: String?,
        val userName: String?,
        val userLogin: String?,
        val isAction: Boolean,
        val message: String?,
        val color: String?,
        val emotes: List<TwitchChatEmote>?,
        val badges: List<Badge>?,
        val inReplyTo: ChatMessage.InReplyTo?
    )

    data class Simple(
        override val data: Data,
        override val timestamp: Instant?
    ) : ChatEntry()

    data class Highlighted(
        val header: String?,
        val headerIconResId: Int? = null,
        override val data: Data?,
        override val timestamp: Instant?
    ) : ChatEntry()
}
