package com.github.andreyasadchy.xtra.ui.chat

import com.github.andreyasadchy.xtra.feature.irc.ChatMessageParser
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.util.chat.LiveChatThread
import com.github.andreyasadchy.xtra.util.chat.LoggedInChatThread
import com.github.andreyasadchy.xtra.util.chat.PubSubListenerImpl
import com.github.andreyasadchy.xtra.util.chat.PubSubWebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock


class LiveChatController(
    user: User.LoggedIn,
    channelId: String?,
    channelLogin: String,
    chatStateListener: ChatViewModel.ChatStateListener,
    coroutineScope: CoroutineScope,
    clock: Clock,
    chatMessageParser: ChatMessageParser
) : ChatController {

    private val liveChat: LiveChatThread =
        LiveChatThread(
            scope = coroutineScope,
            clock = clock,
            channelName = channelLogin,
            listener = chatStateListener.messageListener,
            parser = chatMessageParser
        )

    private val loggedInChat: LoggedInChatThread =
        LoggedInChatThread(
            scope = coroutineScope,
            clock = clock,
            userLogin = user.login,
            userToken = user.helixToken,
            channelName = channelLogin,
            listener = chatStateListener.messageListener,
            parser = chatMessageParser
        )

    private val pubSub: PubSubWebSocket? =
        if (!channelId.isNullOrEmpty()) {
            PubSubWebSocket(
                scope = coroutineScope,
                channelId = channelId,
                listener = PubSubListenerImpl(callback = chatStateListener)
            )
        } else null

    override fun send(message: CharSequence) {
        loggedInChat.send(message)
    }

    override suspend fun start() {
        liveChat.start()
        loggedInChat.start()
        pubSub?.start()
    }

    override fun stop() {
        liveChat.disconnect()
        loggedInChat.disconnect()
        pubSub?.disconnect()
    }
}
