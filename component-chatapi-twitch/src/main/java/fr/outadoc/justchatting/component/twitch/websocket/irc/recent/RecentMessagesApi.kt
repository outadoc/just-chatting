package fr.outadoc.justchatting.component.twitch.websocket.irc.recent

interface RecentMessagesApi {
    suspend fun getRecentMessages(channelLogin: String, limit: Int): RecentMessagesResponse
}
