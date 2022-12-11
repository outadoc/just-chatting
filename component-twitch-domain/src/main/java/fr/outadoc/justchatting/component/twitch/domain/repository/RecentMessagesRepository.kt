package fr.outadoc.justchatting.component.twitch.domain.repository

import fr.outadoc.justchatting.component.twitch.api.RecentMessagesApi
import fr.outadoc.justchatting.component.twitch.model.RecentMessagesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecentMessagesRepository(private val recentMessagesApi: RecentMessagesApi) {

    suspend fun loadRecentMessages(
        channelLogin: String,
        limit: Int
    ): RecentMessagesResponse =
        withContext(Dispatchers.IO) {
            recentMessagesApi.getRecentMessages(channelLogin, limit)
        }
}
