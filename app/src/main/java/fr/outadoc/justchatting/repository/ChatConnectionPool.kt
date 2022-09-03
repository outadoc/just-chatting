package fr.outadoc.justchatting.repository

import android.content.Context
import fr.outadoc.justchatting.ChatConnectionService
import fr.outadoc.justchatting.model.chat.ChatCommand
import fr.outadoc.justchatting.ui.chat.LiveChatController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

class ChatConnectionPool(
    private val applicationContext: Context,
    private val liveChatControllerFactory: LiveChatController.Factory,
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Job())

    private val _chatControllers: MutableMap<String, LiveChatController> = mutableMapOf()

    suspend fun start(channelId: String, channelLogin: String): Flow<ChatCommand> {
        applicationContext.startForegroundService(
            ChatConnectionService.createStartIntent(applicationContext)
        )

        val controller = _chatControllers.getOrPut(channelId) {
            liveChatControllerFactory.create(channelId, channelLogin, coroutineScope)
                .also { thread -> thread.start() }
        }

        return controller.flow
    }

    fun stop(channelId: String) {
        _chatControllers[channelId]?.stop()
        _chatControllers.remove(channelId)
    }

    fun sendMessage(channelId: String, message: CharSequence) {
        _chatControllers[channelId]?.send(message)
    }

    fun dispose() {
        _chatControllers.values.forEach { it.stop() }
        _chatControllers.clear()
    }
}
