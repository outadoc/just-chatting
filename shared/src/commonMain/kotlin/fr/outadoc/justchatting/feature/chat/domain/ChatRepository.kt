package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.coroutines.flow.Flow

internal interface ChatRepository : AutoCloseable {
    fun getChatEventFlow(user: User): Flow<ChatEvent>

    fun getConnectionStatusFlow(user: User): Flow<ConnectionStatus>

    fun start(user: User)
}
