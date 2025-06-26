package fr.outadoc.justchatting.feature.chat.presentation

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Poll
import fr.outadoc.justchatting.feature.chat.domain.model.Prediction
import fr.outadoc.justchatting.feature.chat.domain.model.Raid
import kotlin.time.Instant

@Immutable
internal data class OngoingEvents(
    val poll: Poll? = null,
    val prediction: Prediction? = null,
    val pinnedMessage: PinnedMessage? = null,
    val outgoingRaid: Raid? = null,
) {
    @Immutable
    data class PinnedMessage(
        val message: ChatListItem.Message,
        val endsAt: Instant,
    )
}
