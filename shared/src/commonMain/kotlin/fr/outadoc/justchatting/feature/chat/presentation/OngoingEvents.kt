package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.Poll
import fr.outadoc.justchatting.feature.chat.domain.model.Prediction
import fr.outadoc.justchatting.feature.chat.domain.model.Raid
import kotlinx.datetime.Instant

internal data class OngoingEvents(
    val poll: Poll? = null,
    val prediction: Prediction? = null,
    val pinnedMessage: PinnedMessage? = null,
    val outgoingRaid: Raid? = null,
) {
    data class PinnedMessage(
        val message: ChatEvent.Message,
        val endsAt: Instant,
    )
}
