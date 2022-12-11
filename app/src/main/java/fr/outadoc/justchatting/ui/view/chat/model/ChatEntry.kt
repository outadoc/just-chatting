package fr.outadoc.justchatting.ui.view.chat.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.component.chat.data.model.Badge
import fr.outadoc.justchatting.component.chat.data.model.ChatMessage
import fr.outadoc.justchatting.component.chat.data.model.TwitchChatEmote
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.Instant

@Immutable
sealed class ChatEntry {

    abstract val data: Data?
    abstract val timestamp: Instant

    @Immutable
    data class Data(
        val userId: String?,
        val userName: String,
        val userLogin: String,
        val isAction: Boolean,
        val message: String?,
        val messageId: String?,
        val color: String?,
        val emotes: ImmutableList<TwitchChatEmote>?,
        val badges: ImmutableList<Badge>?,
        val inReplyTo: ChatMessage.InReplyTo?
    )

    @Immutable
    data class Simple(
        override val data: Data,
        override val timestamp: Instant
    ) : ChatEntry()

    @Immutable
    data class Highlighted(
        val header: String?,
        val headerIconResId: Int? = null,
        override val data: Data?,
        override val timestamp: Instant
    ) : ChatEntry()
}
