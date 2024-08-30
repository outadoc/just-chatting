package fr.outadoc.justchatting.feature.chat.domain.model

internal data class ConnectionStatus(
    val isAlive: Boolean = false,
    val registeredListeners: Int = 0,
)
