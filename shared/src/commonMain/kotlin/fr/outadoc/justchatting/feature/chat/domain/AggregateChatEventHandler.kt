package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.feature.chat.domain.handler.ChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.handler.ChatEventHandlersProvider
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.merge

internal class AggregateChatEventHandler(
    chatEventHandlersProvider: ChatEventHandlersProvider,
) : ChatEventHandler {

    private val handlers: List<ChatEventHandler> = chatEventHandlersProvider.get()

    override fun getEventFlow(
        channelId: String,
        channelLogin: String,
        appUser: AppUser.LoggedIn,
    ): Flow<ChatEvent> = handlers.map { it.getEventFlow(channelId, channelLogin, appUser) }.merge()

    override val connectionStatus: Flow<ConnectionStatus> =
        combine(handlers.map { it.connectionStatus }) { statuses ->
            statuses.reduce { acc, status ->
                ConnectionStatus(
                    isAlive = acc.isAlive && status.isAlive,
                    registeredListeners = acc.registeredListeners + status.registeredListeners,
                )
            }
        }.distinctUntilChanged()
}
