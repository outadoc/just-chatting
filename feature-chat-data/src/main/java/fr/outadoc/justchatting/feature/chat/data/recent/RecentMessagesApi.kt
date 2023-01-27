package fr.outadoc.justchatting.feature.chat.data.recent

import fr.outadoc.justchatting.feature.chat.data.model.RecentMessagesResponse

interface RecentMessagesApi {
    suspend fun getRecentMessages(channelLogin: String, limit: Int): RecentMessagesResponse
}
