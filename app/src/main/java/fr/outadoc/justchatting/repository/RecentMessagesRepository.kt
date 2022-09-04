package fr.outadoc.justchatting.repository

import fr.outadoc.justchatting.api.RecentMessagesApi
import fr.outadoc.justchatting.model.chat.RecentMessagesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class RecentMessagesRepository(private val recentMessagesApi: RecentMessagesApi) {

    suspend fun loadRecentMessages(
        channelLogin: String,
        limit: Int
    ): Response<RecentMessagesResponse> =
        withContext(Dispatchers.IO) {
            recentMessagesApi.getRecentMessages(channelLogin, limit)
        }
}
