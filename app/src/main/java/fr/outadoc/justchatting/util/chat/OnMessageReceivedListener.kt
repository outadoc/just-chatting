package fr.outadoc.justchatting.util.chat

import fr.outadoc.justchatting.model.chat.ChatCommand

interface OnMessageReceivedListener {
    fun onCommand(command: ChatCommand)
}
