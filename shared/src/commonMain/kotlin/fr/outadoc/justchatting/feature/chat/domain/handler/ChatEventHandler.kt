package fr.outadoc.justchatting.feature.chat.domain.handler

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow

internal interface ChatEventHandler {
    val eventFlow: Flow<ChatEvent>
    val connectionStatus: Flow<ConnectionStatus>
}
