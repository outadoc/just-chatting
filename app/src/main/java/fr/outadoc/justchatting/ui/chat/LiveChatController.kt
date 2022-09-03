package fr.outadoc.justchatting.ui.chat

import fr.outadoc.justchatting.irc.ChatMessageParser
import fr.outadoc.justchatting.model.User
import fr.outadoc.justchatting.util.chat.LiveChatThread
import fr.outadoc.justchatting.util.chat.LoggedInChatThread
import fr.outadoc.justchatting.util.chat.OnCommandReceivedListener
import fr.outadoc.justchatting.util.chat.PubSubListenerImpl
import fr.outadoc.justchatting.util.chat.PubSubWebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock

class LiveChatController(
    user: User.LoggedIn,
    channelId: String,
    channelLogin: String,
    messageListener: OnCommandReceivedListener,
    coroutineScope: CoroutineScope,
    clock: Clock,
    chatMessageParser: ChatMessageParser
) : ChatController {

    private val liveChat = LiveChatThread(
        scope = coroutineScope,
        clock = clock,
        channelLogin = channelLogin,
        listener = messageListener,
        parser = chatMessageParser
    )

    private val loggedInChat = LoggedInChatThread(
        scope = coroutineScope,
        clock = clock,
        userLogin = user.login,
        userToken = user.helixToken,
        channelName = channelLogin,
        listener = messageListener,
        parser = chatMessageParser
    )

    private val pubSub = PubSubWebSocket(
        scope = coroutineScope,
        channelId = channelId,
        listener = PubSubListenerImpl(callback = messageListener)
    )

    override fun send(message: CharSequence) {
        loggedInChat.send(message)
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
