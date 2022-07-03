package com.github.andreyasadchy.xtra.util.chat

import android.util.Log
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
    private val callbackCommand: OnCommandReceivedListener,
    private val callbackReward: OnRewardReceivedListener,
    private val showUserNotice: Boolean,
    private val showClearMsg: Boolean,
    private val showClearChat: Boolean,
    private val usePubSub: Boolean
) : OnMessageReceivedListener {

    override fun onCommand(command: ChatCommand) {
        Log.d("Chat", "onCommand: $command")
        when (command) {
            is LiveChatMessage -> {
                if (command.rewardId.isNullOrBlank() || !usePubSub) {
                    callback.onMessage(command)
                } else {
                    callbackReward.onReward(command)
                }
            }
            is PubSubPointReward -> {}
            is Command.ClearChat -> if (showClearChat) callbackCommand.onCommand(command)
            is Command.ClearMessage -> if (showClearMsg) callbackCommand.onCommand(command)
            is Command.UserNotice -> if (showUserNotice) callbackCommand.onCommand(command)
            is Command.Notice -> callbackCommand.onCommand(command)
            is Command.Join -> callbackCommand.onCommand(command)
            is Command.Disconnect -> callbackCommand.onCommand(command)
            is Command.SendMessageError -> callbackCommand.onCommand(command)
            is Command.SocketError -> callbackCommand.onCommand(command)
            is RoomState -> callbackRoomState.onRoomState(command)
            is UserState -> callbackUserState.onUserState(command.emoteSets)
            PingCommand -> {}
        }
    }
}
