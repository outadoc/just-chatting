package fr.outadoc.justchatting.feature.chat.domain.handler

internal fun interface ChatEventHandlersProvider {
    public fun get(): List<ChatEventHandler>
}
