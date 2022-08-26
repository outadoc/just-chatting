package com.github.andreyasadchy.xtra.ui.view.chat.model

import com.github.andreyasadchy.xtra.model.chat.Badge
import com.github.andreyasadchy.xtra.model.chat.LiveChatMessage
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import kotlinx.datetime.Instant

sealed class ChatEntry {

    abstract val data: Data?
    abstract val timestamp: Instant?

    sealed interface Data {

        class Rich(
            val userId: String?,
            val userName: String?,
            val userLogin: String?,
            val isAction: Boolean,
            val message: String?,
            val color: String?,
            val emotes: List<TwitchEmote>?,
            val badges: List<Badge>?,
            val inReplyTo: LiveChatMessage.InReplyTo?,
        ) : Data

        class Plain(
            val message: String?,
        ) : Data
    }

    data class Simple(
        override val data: Data,
        override val timestamp: Instant?,
    ) : ChatEntry()

    data class Highlighted(
        val header: String?,
        val headerIconResId: Int? = null,
        override val data: Data?,
        override val timestamp: Instant?,
    ) : ChatEntry()
}
