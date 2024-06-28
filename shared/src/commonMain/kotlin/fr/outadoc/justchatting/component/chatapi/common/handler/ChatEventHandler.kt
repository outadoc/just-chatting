package fr.outadoc.justchatting.component.chatapi.common.handler

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.ConnectionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

internal interface ChatEventHandler {

    val commandFlow: Flow<ChatEvent>
    val connectionStatus: StateFlow<ConnectionStatus>

    fun start()
    fun disconnect()
    fun send(message: CharSequence, inReplyToId: String?)
}
