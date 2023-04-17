package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.component.chatapi.common.ConnectionStatus
import fr.outadoc.justchatting.component.chatapi.common.handler.ChatEventHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

class DefaultChatRepository(
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
                        preventSendingMessages = acc.preventSendingMessages || status.preventSendingMessages,
                        registeredListeners = acc.registeredListeners + status.registeredListeners,
                    )
                }
            }
        }
        .distinctUntilChanged()

    override fun start(channelId: String, channelLogin: String): HandlerResult {
        val handler: ChatEventHandler = handlers.value[channelId]
            ?: factory.create(
                channelId = channelId,
                channelLogin = channelLogin,
                coroutineScope = coroutineScope,
            ).also { thread ->
                thread.start()
            }

        handlers.update { current ->
            current + (channelId to handler)
        }

        return HandlerResult(
            commandFlow = handler.commandFlow,
            connectionStatus = handler.connectionStatus,
        )
    }

    override fun stop(channelId: String) {
        handlers.value[channelId]?.disconnect()
    }

    override fun sendMessage(channelId: String, message: CharSequence, inReplyToId: String?) {
        handlers.value[channelId]?.send(message, inReplyToId)
    }

    override fun dispose() {
        handlers.value.forEach { (_, handler) -> handler.disconnect() }
    }
}
