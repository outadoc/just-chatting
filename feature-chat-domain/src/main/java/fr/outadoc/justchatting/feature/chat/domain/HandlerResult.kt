package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.ConnectionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class HandlerResult(
    val commandFlow: Flow<ChatEvent>,
    val connectionStatus: StateFlow<ConnectionStatus>,
)
