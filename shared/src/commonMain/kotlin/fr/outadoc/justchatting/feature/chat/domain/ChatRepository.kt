package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow

internal interface ChatRepository {

    val connectionStatus: Flow<ConnectionStatus>

    fun getChatEventFlow(channelId: String, channelLogin: String): Flow<ChatEvent>
    fun getConnectionStatusFlow(channelId: String, channelLogin: String): Flow<ConnectionStatus>
}
