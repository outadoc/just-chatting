package fr.outadoc.justchatting.feature.chat.domain.handler

public fun interface ChatEventHandlersProvider {
    public fun get(): List<ChatEventHandler>
}
