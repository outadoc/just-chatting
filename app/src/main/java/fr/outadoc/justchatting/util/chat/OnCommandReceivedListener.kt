package fr.outadoc.justchatting.util.chat

import fr.outadoc.justchatting.model.chat.ChatCommand

interface OnCommandReceivedListener {
    fun onCommand(command: ChatCommand)
}
