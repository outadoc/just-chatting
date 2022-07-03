package com.github.andreyasadchy.xtra.util.chat

import com.github.andreyasadchy.xtra.model.chat.Command

interface OnCommandReceivedListener {
    fun onCommand(command: Command)
}
