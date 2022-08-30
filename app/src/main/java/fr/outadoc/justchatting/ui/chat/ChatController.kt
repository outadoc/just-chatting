package fr.outadoc.justchatting.ui.chat

interface ChatController {
    fun send(message: CharSequence)
    suspend fun start()
    fun stop()
}
