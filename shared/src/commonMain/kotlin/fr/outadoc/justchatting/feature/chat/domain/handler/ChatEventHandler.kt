package fr.outadoc.justchatting.feature.chat.domain.handler

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.coroutines.flow.Flow

internal interface ChatEventHandler {
    public fun getEventFlow(
        channelId: String,
        channelLogin: String,
        appUser: AppUser.LoggedIn,
    ): Flow<ChatEvent>

    public val connectionStatus: Flow<ConnectionStatus>
}
