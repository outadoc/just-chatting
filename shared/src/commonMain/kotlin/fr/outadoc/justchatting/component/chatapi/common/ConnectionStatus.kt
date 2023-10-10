package fr.outadoc.justchatting.component.chatapi.common

data class ConnectionStatus(
    val isAlive: Boolean = false,
    val preventSendingMessages: Boolean = true,
    val registeredListeners: Int = 0,
)
