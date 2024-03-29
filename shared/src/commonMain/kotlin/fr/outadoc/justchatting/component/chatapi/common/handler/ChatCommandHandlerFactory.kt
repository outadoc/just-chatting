package fr.outadoc.justchatting.component.chatapi.common.handler

import kotlinx.coroutines.CoroutineScope

interface ChatCommandHandlerFactory {
    fun create(scope: CoroutineScope, channelLogin: String, channelId: String): ChatEventHandler
}
