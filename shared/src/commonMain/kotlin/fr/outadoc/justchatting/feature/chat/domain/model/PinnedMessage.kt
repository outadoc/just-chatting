package fr.outadoc.justchatting.feature.chat.domain.model

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
internal data class PinnedMessage(
    val pinId: String,
    val pinnedBy: User,
    val message: Message,
) {
    @Immutable
    data class User(
        val userId: String,
        val displayName: String,
    )

    @Immutable
    data class Message(
        val messageId: String,
        val sender: User,
        val content: Content,
        val startsAt: Instant,
        val endsAt: Instant,
    ) {
        @Immutable
        data class Content(
            val text: String,
        )
    }
}
