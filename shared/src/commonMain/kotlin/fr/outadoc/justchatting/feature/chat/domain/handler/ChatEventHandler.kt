package fr.outadoc.justchatting.feature.chat.domain.handler

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

internal interface ChatEventHandler {

    val commandFlow: Flow<ChatEvent>
    val connectionStatus: StateFlow<ConnectionStatus>

    fun start()
    fun disconnect()
    fun send(message: CharSequence, inReplyToId: String?)
}
