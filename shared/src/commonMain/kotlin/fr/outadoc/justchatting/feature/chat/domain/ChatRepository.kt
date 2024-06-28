package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.ConnectionStatus
import kotlinx.coroutines.flow.Flow

internal interface ChatRepository {

    val connectionStatus: Flow<ConnectionStatus>

    fun getChatEventFlow(channelId: String, channelLogin: String): Flow<ChatEvent>
    fun getConnectionStatusFlow(channelId: String, channelLogin: String): Flow<ConnectionStatus>
    fun start(channelId: String, channelLogin: String)
    fun stop(channelId: String)
    fun sendMessage(channelId: String, message: CharSequence, inReplyToId: String? = null)
    fun dispose()
}
