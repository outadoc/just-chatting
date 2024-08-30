package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.feature.chat.domain.handler.ChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

internal class DefaultChatRepository(
    private val factory: AggregateChatEventHandler.Factory,
) : ChatRepository {

    private val coroutineScope: CoroutineScope = CoroutineScope(Job())

    private val handlers = MutableStateFlow(emptyMap<String, ChatEventHandler>())

    @OptIn(ExperimentalCoroutinesApi::class)
    override val connectionStatus: Flow<ConnectionStatus> = handlers
        .flatMapLatest { current ->
            combine(current.map { handler -> handler.value.connectionStatus }) { statuses ->
                statuses.reduce { acc, status ->
                    ConnectionStatus(
                        isAlive = acc.isAlive && status.isAlive,
                        registeredListeners = acc.registeredListeners + status.registeredListeners,
                    )
                }
            }
        }
        .distinctUntilChanged()

    override fun getChatEventFlow(channelId: String, channelLogin: String): Flow<ChatEvent> {
        return getOrCreateEventHandler(channelId, channelLogin).eventFlow
    }

    override fun getConnectionStatusFlow(
        channelId: String,
        channelLogin: String,
    ): Flow<ConnectionStatus> {
        return getOrCreateEventHandler(channelId, channelLogin).connectionStatus
    }

    private fun getOrCreateEventHandler(channelId: String, channelLogin: String): ChatEventHandler {
        val handler: ChatEventHandler = handlers.value[channelId]
            ?: factory.create(
                channelId = channelId,
                channelLogin = channelLogin,
                coroutineScope = coroutineScope,
            )

        handlers.update { current ->
            current + (channelId to handler)
        }

        return handler
    }
}
