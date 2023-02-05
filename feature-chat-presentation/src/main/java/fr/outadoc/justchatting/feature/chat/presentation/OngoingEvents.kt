package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.component.chatapi.common.Poll

data class OngoingEvents(
    val poll: Poll? = null,
)
