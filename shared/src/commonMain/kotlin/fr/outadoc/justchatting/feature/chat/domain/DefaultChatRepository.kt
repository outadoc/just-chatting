package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.feature.chat.domain.handler.ChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate

internal class DefaultChatRepository(
    private val factory: AggregateChatEventHandler.Factory,
) : ChatRepository {
    private val coroutineScope: CoroutineScope = CoroutineScope(Job())

    private val handlerFlow: MutableStateFlow<ChatEventHandler?> = MutableStateFlow(null)

    override fun getChatEventFlow(user: User): Flow<ChatEvent> {
        return getOrCreateEventHandler(user).eventFlow
    }

    override fun getConnectionStatusFlow(
        user: User,
    ): Flow<ConnectionStatus> {
        return getOrCreateEventHandler(user).connectionStatus
    }

    private fun getOrCreateEventHandler(user: User): ChatEventHandler {
        val handler: ChatEventHandler =
            handlerFlow.value
                ?: factory
                    .create(
                        channelId = user.id,
                        channelLogin = user.login,
                        coroutineScope = coroutineScope,
                    ).also { thread ->
                        thread.start()
                    }

        return handler.also {
            handlerFlow.value = it
        }
    }

    override fun start(user: User) {
        getOrCreateEventHandler(user).start()
    }

    override fun close() {
        handlerFlow.getAndUpdate { null }?.disconnect()
    }
}
