package fr.outadoc.justchatting.feature.chat.domain.model

internal data class ConnectionStatus(
    val isAlive: Boolean = false,
    val preventSendingMessages: Boolean = true,
    val registeredListeners: Int = 0,
)
