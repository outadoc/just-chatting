package com.github.andreyasadchy.xtra.util.chat

interface OnCommandReceivedListener {
    fun onCommand(list: Command)
}

data class Command(
    val message: String,
    val type: String? = null)