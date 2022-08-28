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
    private val callbackRoomState: OnRoomStateReceivedListener,
    private val showUserNotice: Boolean,
    private val showClearMsg: Boolean,
    private val showClearChat: Boolean,
) : OnMessageReceivedListener {

    override fun onCommand(command: ChatCommand) {
        when (command) {
            is LiveChatMessage -> callback.onMessage(command)
            is Command.ClearChat -> if (showClearChat) callback.onMessage(command)
            is Command.ClearMessage -> if (showClearMsg) callback.onMessage(command)
            is Command.UserNotice -> if (showUserNotice) callback.onMessage(command)
            is Command.Notice -> callback.onMessage(command)
            is Command.Join -> callback.onMessage(command)
            is Command.Disconnect -> callback.onMessage(command)
            is Command.SendMessageError -> callback.onMessage(command)
            is Command.SocketError -> callback.onMessage(command)
            is RoomState -> callbackRoomState.onRoomState(command)
            is UserState -> callbackUserState.onUserState(command.emoteSets)
            is PubSubPointReward,
            is Command.Ban,
            is Command.Timeout,
            PingCommand -> Unit
        }
    }
}
