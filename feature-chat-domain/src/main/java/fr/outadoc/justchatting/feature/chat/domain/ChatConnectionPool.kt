package fr.outadoc.justchatting.feature.chat.domain

import fr.outadoc.justchatting.feature.chat.data.ChatCommandHandler
import fr.outadoc.justchatting.feature.chat.data.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class ChatConnectionPool(
    private val factory: AggregateChatCommandHandler.Factory,
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Job())
    private val handlers: MutableMap<String, ChatCommandHandler> = mutableMapOf()

    class HandlerResult(
        val commandFlow: Flow<ChatCommand>,
        val connectionStatus: StateFlow<ConnectionStatus>,
    )

    val hasActiveThreads: Boolean
        get() = handlers.map { handler -> handler.value.connectionStatus.value }
            .any { status -> status.isAlive }

    fun start(channelId: String, channelLogin: String): HandlerResult {
        val handler = handlers.getOrPut(channelId) {
            factory.create(channelId, channelLogin, coroutineScope)
                .also { thread -> thread.start() }
        }

        return HandlerResult(
            commandFlow = handler.commandFlow,
            connectionStatus = handler.connectionStatus,
        )
    }

    fun stop(channelId: String) {
        handlers[channelId]?.disconnect()
    }

    fun sendMessage(channelId: String, message: CharSequence, inReplyToId: String? = null) {
        handlers[channelId]?.send(message, inReplyToId)
    }

    fun dispose() {
        handlers.values.forEach { handler -> handler.disconnect() }
    }
}
