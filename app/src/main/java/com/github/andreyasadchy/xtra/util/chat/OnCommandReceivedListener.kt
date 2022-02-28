package com.github.andreyasadchy.xtra.util.chat

import com.github.andreyasadchy.xtra.model.chat.TwitchEmote

interface OnCommandReceivedListener {
    fun onCommand(list: Command)
}

data class Command(
    val message: String? = null,
    val duration: String? = null,
    val type: String? = null,
    val emotes: List<TwitchEmote>? = null,
    val timestamp: Long? = null,
    val fullMsg: String? = null)