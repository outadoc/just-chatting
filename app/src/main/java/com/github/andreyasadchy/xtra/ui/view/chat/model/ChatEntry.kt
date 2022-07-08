package com.github.andreyasadchy.xtra.ui.view.chat.model

import com.github.andreyasadchy.xtra.model.chat.Badge
import com.github.andreyasadchy.xtra.model.chat.RemoteImage
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import kotlinx.datetime.Instant

sealed class ChatEntry {

    abstract val data: Data

    sealed interface Data {

        val timestamp: Instant?

        class Rich(
            val userId: String?,
            val userName: String?,
            val userLogin: String?,
            val isAction: Boolean,
            val message: String?,
            val color: String?,
            val emotes: List<TwitchEmote>?,
            val badges: List<Badge>?,
            override val timestamp: Instant?
        ) : Data

        class Simple(
            val message: String?,
            override val timestamp: Instant?
        ) : Data
    }

    data class Plain(
        override val data: Data
    ) : ChatEntry()

    data class WithHeader(
        val header: String?,
        val headerImage: RemoteImage? = null,
        override val data: Data
    ) : ChatEntry()
}
