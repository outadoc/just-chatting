package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.Poll
import fr.outadoc.justchatting.component.chatapi.common.Prediction
import fr.outadoc.justchatting.component.chatapi.common.Raid
import kotlinx.datetime.Instant

data class OngoingEvents(
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
