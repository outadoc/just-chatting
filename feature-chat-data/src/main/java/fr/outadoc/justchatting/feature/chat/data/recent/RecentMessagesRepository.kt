package fr.outadoc.justchatting.feature.chat.data.recent

import fr.outadoc.justchatting.feature.chat.data.model.RecentMessagesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecentMessagesRepository(private val recentMessagesApi: RecentMessagesApi) {

    suspend fun loadRecentMessages(channelLogin: String, limit: Int): RecentMessagesResponse =
        withContext(Dispatchers.IO) {
            recentMessagesApi.getRecentMessages(channelLogin, limit)
        }
}
