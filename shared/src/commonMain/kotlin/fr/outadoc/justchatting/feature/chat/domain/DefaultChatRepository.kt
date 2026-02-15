package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.feature.chat.domain.handler.ChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class DefaultChatRepository(
    private val factory: AggregateChatEventHandler.Factory,
) : ChatRepository {
    private val handlerFlow: MutableStateFlow<ChatEventHandler?> = MutableStateFlow(null)

    override fun getChatEventFlow(
        user: User,
        appUser: AppUser.LoggedIn,
    ): Flow<ChatEvent> = getOrCreateEventHandler(user, appUser).eventFlow

    override fun getConnectionStatusFlow(
        user: User,
        appUser: AppUser.LoggedIn,
    ): Flow<ConnectionStatus> = getOrCreateEventHandler(user, appUser).connectionStatus

    private fun getOrCreateEventHandler(
        user: User,
        appUser: AppUser.LoggedIn,
    ): ChatEventHandler {
        val handler: ChatEventHandler =
            handlerFlow.value
                ?: factory.create(
                    channelId = user.id,
                    channelLogin = user.login,
                    appUser = appUser,
                )

        return handler.also {
            handlerFlow.value = it
        }
    }
}
