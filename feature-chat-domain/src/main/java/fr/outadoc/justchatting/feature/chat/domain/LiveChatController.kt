package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.feature.chat.data.ChatCommandHandler
import fr.outadoc.justchatting.feature.chat.data.ChatCommandHandlerFactoriesProvider
import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge

class LiveChatController(
    channelId: String,
    channelLogin: String,
    coroutineScope: CoroutineScope,
    chatCommandHandlerFactoriesProvider: ChatCommandHandlerFactoriesProvider
) : ChatController {

    class Factory(
        private val chatCommandHandlerFactoriesProvider: ChatCommandHandlerFactoriesProvider
    ) {
        fun create(
            channelId: String,
            channelLogin: String,
            coroutineScope: CoroutineScope,
        ): LiveChatController {
            return LiveChatController(
                channelId = channelId,
                channelLogin = channelLogin,
                coroutineScope = coroutineScope,
                chatCommandHandlerFactoriesProvider = chatCommandHandlerFactoriesProvider
            )
        }
    }

    private val handlers: List<ChatCommandHandler> =
        chatCommandHandlerFactoriesProvider.get().map { handlerFactory ->
            handlerFactory.create(
                scope = coroutineScope,
                channelLogin = channelLogin,
                channelId = channelId
            )
        }

    val flow: Flow<ChatCommand> = handlers.map { handler -> handler.commandFlow }.merge()

    override fun send(message: CharSequence, inReplyToId: String?) {
        handlers.forEach { handler -> handler.send(message, inReplyToId) }
    }

    override suspend fun start() {
        handlers.forEach { handler -> handler.start() }
    }

    override fun stop() {
        handlers.forEach { handler -> handler.disconnect() }
    }
}
