package fr.outadoc.justchatting.feature.chat.data

fun interface ChatCommandHandlerFactoriesProvider {
    fun get(): List<ChatCommandHandlerFactory>
}