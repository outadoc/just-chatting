package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.feature.chat.domain.handler.ChatCommandHandlerFactoriesProvider
import fr.outadoc.justchatting.feature.chat.domain.handler.ChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn

internal class AggregateChatEventHandler(
    channelId: String,
    channelLogin: String,
    coroutineScope: CoroutineScope,
    chatCommandHandlerFactoriesProvider: ChatCommandHandlerFactoriesProvider,
) : ChatEventHandler {

    class Factory(
        private val chatCommandHandlerFactoriesProvider: ChatCommandHandlerFactoriesProvider,
    ) {
        fun create(
            channelId: String,
            channelLogin: String,
            coroutineScope: CoroutineScope,
        ): ChatEventHandler {
            return AggregateChatEventHandler(
                channelId = channelId,
                channelLogin = channelLogin,
                coroutineScope = coroutineScope,
                chatCommandHandlerFactoriesProvider = chatCommandHandlerFactoriesProvider,
            )
        }
    }

    private val handlers: List<ChatEventHandler> =
        chatCommandHandlerFactoriesProvider.get().map { handlerFactory ->
            handlerFactory.create(
                scope = coroutineScope,
                channelLogin = channelLogin,
                channelId = channelId,
            )
        }

    override val eventFlow: Flow<ChatEvent> =
        handlers.map { handler -> handler.eventFlow }.merge()

    override val connectionStatus: StateFlow<ConnectionStatus> =
        combine(handlers.map { handler -> handler.connectionStatus }) { statuses ->
            statuses.reduce { acc, status ->
                ConnectionStatus(
                    isAlive = acc.isAlive && status.isAlive,
                    preventSendingMessages = acc.preventSendingMessages || status.preventSendingMessages,
                    registeredListeners = acc.registeredListeners + status.registeredListeners,
                )
            }
        }
            .distinctUntilChanged()
            .stateIn(
                coroutineScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = ConnectionStatus(),
            )
}
