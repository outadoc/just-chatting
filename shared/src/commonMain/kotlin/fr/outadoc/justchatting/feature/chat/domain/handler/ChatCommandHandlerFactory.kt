package fr.outadoc.justchatting.feature.chat.domain.handler

import kotlinx.coroutines.CoroutineScope

internal interface ChatCommandHandlerFactory {
    fun create(scope: CoroutineScope, channelLogin: String, channelId: String): ChatEventHandler
}
