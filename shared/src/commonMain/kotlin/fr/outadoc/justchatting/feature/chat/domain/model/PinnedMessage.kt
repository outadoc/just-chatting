package fr.outadoc.justchatting.feature.chat.domain.model

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
public data class PinnedMessage(
    val pinId: String,
    val pinnedBy: User,
    val message: Message,
) {
    @Immutable
    public data class User(
        val userId: String,
        val displayName: String,
    )

    @Immutable
    public data class Message(
        val messageId: String,
        val sender: User,
        val content: Content,
        val startsAt: Instant,
        val endsAt: Instant,
    ) {
        @Immutable
        public data class Content(
            val text: String,
        )
    }
}
