package fr.outadoc.justchatting.component.chat.domain

interface ChatController {
    fun send(message: CharSequence, inReplyToId: String?)
    suspend fun start()
    fun stop()
}
