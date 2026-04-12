package fr.outadoc.justchatting.feature.chat.domain.model

public data class ConnectionStatus(
    val isAlive: Boolean = false,
    val registeredListeners: Int = 0,
)
