package fr.outadoc.justchatting.model.chat

data class EmoteCard(
    val id: String?,
    val name: String?,
    val type: String?,
    val subTier: String?,
    val bitThreshold: Int?,
    val channelId: String?,
    val channelLogin: String?,
    val channelName: String?
)
