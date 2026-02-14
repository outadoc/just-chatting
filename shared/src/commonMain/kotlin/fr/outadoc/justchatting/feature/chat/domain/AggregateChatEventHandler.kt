package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.feature.chat.domain.handler.ChatCommandHandlerFactoriesProvider
import fr.outadoc.justchatting.feature.chat.domain.handler.ChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.merge

internal class AggregateChatEventHandler(
    channelId: String,
    channelLogin: String,
    appUser: AppUser.LoggedIn,
    chatCommandHandlerFactoriesProvider: ChatCommandHandlerFactoriesProvider,
) : ChatEventHandler {
    class Factory(
        private val chatCommandHandlerFactoriesProvider: ChatCommandHandlerFactoriesProvider,
    ) {
        fun create(
            channelId: String,
            channelLogin: String,
            appUser: AppUser.LoggedIn,
        ): ChatEventHandler = AggregateChatEventHandler(
            channelId = channelId,
            channelLogin = channelLogin,
            appUser = appUser,
            chatCommandHandlerFactoriesProvider = chatCommandHandlerFactoriesProvider,
        )
    }

    private val handlers: List<ChatEventHandler> =
        chatCommandHandlerFactoriesProvider.get().map { handlerFactory ->
            handlerFactory.create(
                channelLogin = channelLogin,
                channelId = channelId,
                appUser = appUser,
            )
        }

    override val eventFlow: Flow<ChatEvent> =
        handlers.map { handler -> handler.eventFlow }.merge()

    override val connectionStatus: Flow<ConnectionStatus> =
        combine(handlers.map { handler -> handler.connectionStatus }) { statuses ->
            statuses.reduce { acc, status ->
                ConnectionStatus(
                    isAlive = acc.isAlive && status.isAlive,
                    registeredListeners = acc.registeredListeners + status.registeredListeners,
                )
            }
        }.distinctUntilChanged()

    override fun start() {
        handlers.forEach { handler -> handler.start() }
    }

    override fun disconnect() {
        handlers.forEach { handler -> handler.disconnect() }
    }
}
