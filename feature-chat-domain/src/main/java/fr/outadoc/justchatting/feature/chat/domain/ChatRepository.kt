package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.component.chatapi.common.ConnectionStatus
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    val connectionStatus: Flow<ConnectionStatus>

    fun start(channelId: String, channelLogin: String): HandlerResult
    fun stop(channelId: String)
    fun sendMessage(channelId: String, message: CharSequence, inReplyToId: String? = null)
    fun dispose()
}
