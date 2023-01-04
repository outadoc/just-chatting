package fr.outadoc.justchatting.feature.chat.data

import kotlinx.coroutines.CoroutineScope

interface ChatCommandHandlerFactory {
    fun create(scope: CoroutineScope, channelLogin: String, channelId: String): ChatCommandHandler
}
