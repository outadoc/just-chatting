package fr.outadoc.justchatting.feature.chat.data

import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatCommandHandler {

    val commandFlow: Flow<ChatCommand>
    val connectionStatus: StateFlow<ConnectionStatus>

    fun start()
    fun disconnect()
    fun send(message: CharSequence, inReplyToId: String?)
}
