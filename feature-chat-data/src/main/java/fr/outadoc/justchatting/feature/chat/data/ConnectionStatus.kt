package fr.outadoc.justchatting.feature.chat.data

data class ConnectionStatus(
    val isAlive: Boolean = false,
    val preventSendingMessages: Boolean = true,
)
