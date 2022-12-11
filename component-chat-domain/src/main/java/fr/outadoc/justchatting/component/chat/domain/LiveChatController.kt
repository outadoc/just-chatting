package fr.outadoc.justchatting.component.chat.domain

import fr.outadoc.justchatting.component.chat.data.model.ChatCommand
import fr.outadoc.justchatting.component.chat.data.websocket.LiveChatWebSocket
import fr.outadoc.justchatting.component.chat.data.websocket.LoggedInChatWebSocket
import fr.outadoc.justchatting.component.chat.data.websocket.PubSubWebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge

class LiveChatController(
    channelId: String,
    channelLogin: String,
    coroutineScope: CoroutineScope,
    liveChatThreadFactory: LiveChatWebSocket.Factory,
    loggedInChatThreadFactory: LoggedInChatWebSocket.Factory,
    pubSubWebSockerFactory: PubSubWebSocket.Factory
) : ChatController {

    class Factory(
        private val liveChatThreadFactory: LiveChatWebSocket.Factory,
        private val loggedInChatThreadFactory: LoggedInChatWebSocket.Factory,
        private val pubSubWebSockerFactory: PubSubWebSocket.Factory
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
                liveChatThreadFactory = liveChatThreadFactory,
                loggedInChatThreadFactory = loggedInChatThreadFactory,
                pubSubWebSockerFactory = pubSubWebSockerFactory
            )
        }
    }

    private val liveChat = liveChatThreadFactory.create(coroutineScope, channelLogin)
    private val loggedInChat = loggedInChatThreadFactory.create(coroutineScope, channelLogin)
    private val pubSub = pubSubWebSockerFactory.create(coroutineScope, channelId)

    val flow: Flow<ChatCommand> = merge(
        liveChat.flow,
        loggedInChat.flow,
        pubSub.flow
    )

    override fun send(message: CharSequence, inReplyToId: String?) {
        loggedInChat.send(message, inReplyToId)
    }

    override suspend fun start() {
        liveChat.start()
        loggedInChat.start()
        pubSub.start()
    }

    override fun stop() {
        liveChat.disconnect()
        loggedInChat.disconnect()
        pubSub.disconnect()
    }
}
