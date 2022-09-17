package fr.outadoc.justchatting.ui.chat

interface ChatController {
    fun send(message: CharSequence, inReplyToId: String?)
    suspend fun start()
    fun stop()
}
