package fr.outadoc.justchatting.feature.chat.data.irc.recent

internal interface RecentMessagesApi {
    suspend fun getRecentMessages(
        channelLogin: String,
        limit: Int,
    ): Result<RecentMessagesResponse>
}
