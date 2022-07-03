package com.github.andreyasadchy.xtra.util.chat

import com.github.andreyasadchy.xtra.model.chat.ChatCommand

interface OnMessageReceivedListener {
    fun onCommand(command: ChatCommand)
}
