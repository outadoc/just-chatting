package fr.outadoc.justchatting.feature.chat.domain.handler

import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser

internal interface ChatCommandHandlerFactory {
    fun create(
        channelLogin: String,
        channelId: String,
        appUser: AppUser.LoggedIn,
    ): ChatEventHandler
}
