package fr.outadoc.justchatting.feature.chat.domain.handler

internal fun interface ChatEventHandlersProvider {
    fun get(): List<ChatEventHandler>
}
