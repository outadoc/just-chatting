package fr.outadoc.justchatting.component.chatapi.common.handler

fun interface ChatCommandHandlerFactoriesProvider {
    fun get(): List<ChatCommandHandlerFactory>
}
