package fr.outadoc.justchatting.feature.chat.domain

interface ChatController {
    fun send(message: CharSequence, inReplyToId: String?)
    suspend fun start()
    fun stop()
}
