package fr.outadoc.justchatting.util.chat

import fr.outadoc.justchatting.model.chat.ChatCommand
import fr.outadoc.justchatting.model.chat.Command
import fr.outadoc.justchatting.model.chat.LiveChatMessage
import fr.outadoc.justchatting.model.chat.PingCommand
import fr.outadoc.justchatting.model.chat.PubSubPointReward
import fr.outadoc.justchatting.model.chat.RoomState
import fr.outadoc.justchatting.model.chat.UserState

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
