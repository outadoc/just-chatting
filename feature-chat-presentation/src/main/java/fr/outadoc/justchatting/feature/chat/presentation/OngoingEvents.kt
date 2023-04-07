package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.component.chatapi.common.PinnedMessage
import fr.outadoc.justchatting.component.chatapi.common.Poll
import fr.outadoc.justchatting.component.chatapi.common.Prediction
import fr.outadoc.justchatting.component.chatapi.common.Raid

data class OngoingEvents(
    val poll: Poll? = null,
    val prediction: Prediction? = null,
    val pinnedMessage: PinnedMessage? = null,
    val outgoingRaid: Raid? = null,
)
