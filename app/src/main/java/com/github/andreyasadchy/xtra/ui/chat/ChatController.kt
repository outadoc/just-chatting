package com.github.andreyasadchy.xtra.ui.chat

interface ChatController {
    fun send(message: CharSequence)
    suspend fun start()
    fun stop()
}
