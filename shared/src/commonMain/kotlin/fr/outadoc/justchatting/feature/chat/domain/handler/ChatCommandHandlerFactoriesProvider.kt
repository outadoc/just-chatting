package fr.outadoc.justchatting.feature.chat.domain.handler

internal fun interface ChatCommandHandlerFactoriesProvider {
    fun get(): List<ChatCommandHandlerFactory>
}
