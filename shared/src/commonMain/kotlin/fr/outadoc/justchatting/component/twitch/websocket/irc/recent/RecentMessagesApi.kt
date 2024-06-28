package fr.outadoc.justchatting.component.twitch.websocket.irc.recent

internal interface RecentMessagesApi {
    suspend fun getRecentMessages(channelLogin: String, limit: Int): Result<RecentMessagesResponse>
}
