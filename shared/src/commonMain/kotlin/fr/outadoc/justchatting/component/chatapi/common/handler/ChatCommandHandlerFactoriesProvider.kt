package fr.outadoc.justchatting.component.chatapi.common.handler

internal fun interface ChatCommandHandlerFactoriesProvider {
    fun get(): List<ChatCommandHandlerFactory>
}
