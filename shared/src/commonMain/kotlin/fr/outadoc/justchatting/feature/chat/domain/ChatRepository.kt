package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.coroutines.flow.Flow

internal interface ChatRepository {
    fun getChatEventFlow(
        user: User,
        appUser: AppUser.LoggedIn,
    ): Flow<ChatEvent>

    fun getConnectionStatusFlow(
        user: User,
        appUser: AppUser.LoggedIn,
    ): Flow<ConnectionStatus>
}
