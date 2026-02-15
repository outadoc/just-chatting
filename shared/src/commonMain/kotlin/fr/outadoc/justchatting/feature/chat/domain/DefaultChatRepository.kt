package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.coroutines.flow.Flow

internal class DefaultChatRepository(
    private val handler: AggregateChatEventHandler,
) : ChatRepository {

    override fun getChatEventFlow(
        user: User,
        appUser: AppUser.LoggedIn,
    ): Flow<ChatEvent> = handler.getEventFlow(
        channelId = user.id,
        channelLogin = user.login,
        appUser = appUser,
    )

    override fun getConnectionStatusFlow(
        user: User,
        appUser: AppUser.LoggedIn,
    ): Flow<ConnectionStatus> = handler.connectionStatus
}
