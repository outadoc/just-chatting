package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.feature.chat.data.ChatCommandHandler
import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

class ChatConnectionPool(
    private val factory: AggregateChatCommandHandler.Factory,
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Job())
    private val handlers: MutableMap<String, ChatCommandHandler> = mutableMapOf()

    val hasActiveThreads: Boolean
        get() = handlers.isNotEmpty()

    fun start(channelId: String, channelLogin: String): Flow<ChatCommand> {
        val handler = handlers.getOrPut(channelId) {
            factory.create(channelId, channelLogin, coroutineScope)
                .also { thread -> thread.start() }
        }

        return handler.commandFlow
    }

    fun stop(channelId: String) {
        handlers[channelId]?.disconnect()
        handlers.remove(channelId)
    }

    fun sendMessage(channelId: String, message: CharSequence, inReplyToId: String? = null) {
        handlers[channelId]?.send(message, inReplyToId)
    }

    fun dispose() {
        handlers.values.forEach { it.disconnect() }
        handlers.clear()
    }
}
