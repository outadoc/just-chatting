package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.component.chatapi.common.Poll
import fr.outadoc.justchatting.component.chatapi.common.Prediction

data class OngoingEvents(
    val poll: Poll? = null,
    val prediction: Prediction? = null,
)
