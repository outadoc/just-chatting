package fr.outadoc.justchatting.util.chat

import fr.outadoc.justchatting.model.chat.ChatCommand

interface OnChatMessageReceivedListener {
    fun onMessage(message: ChatCommand)
}
