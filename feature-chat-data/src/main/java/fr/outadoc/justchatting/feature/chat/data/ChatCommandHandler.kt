package fr.outadoc.justchatting.feature.chat.data

import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import kotlinx.coroutines.flow.Flow

interface ChatCommandHandler {
    val commandFlow: Flow<ChatCommand>
    fun send(message: CharSequence, inReplyToId: String?)
    fun start()
    fun disconnect()
}
