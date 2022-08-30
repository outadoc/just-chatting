package com.github.andreyasadchy.xtra.util.chat

import com.github.andreyasadchy.xtra.model.chat.ChatCommand
import com.github.andreyasadchy.xtra.model.chat.Command
import com.github.andreyasadchy.xtra.model.chat.LiveChatMessage
import com.github.andreyasadchy.xtra.model.chat.PingCommand
import com.github.andreyasadchy.xtra.model.chat.PubSubPointReward
import com.github.andreyasadchy.xtra.model.chat.RoomState
import com.github.andreyasadchy.xtra.model.chat.UserState

class MessageListenerImpl(
    private val callback: OnChatMessageReceivedListener,
    private val callbackUserState: OnUserStateReceivedListener,
    private val callbackRoomState: OnRoomStateReceivedListener
) : OnMessageReceivedListener {

    override fun onCommand(command: ChatCommand) {
        when (command) {
            is LiveChatMessage -> callback.onMessage(command)
            is Command.ClearChat -> callback.onMessage(command)
            is Command.ClearMessage -> callback.onMessage(command)
            is Command.UserNotice -> callback.onMessage(command)
            is Command.Notice -> callback.onMessage(command)
            is Command.Join -> callback.onMessage(command)
            is Command.Disconnect -> callback.onMessage(command)
            is Command.SendMessageError -> callback.onMessage(command)
            is Command.SocketError -> callback.onMessage(command)
            is RoomState -> callbackRoomState.onRoomState(command)
            is UserState -> callbackUserState.onUserState(command)
            is PubSubPointReward,
            is Command.Ban,
            is Command.Timeout,
            PingCommand -> Unit
        }
    }
}
