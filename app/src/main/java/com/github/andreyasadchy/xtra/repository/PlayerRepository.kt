package com.github.andreyasadchy.xtra.repository

import com.github.andreyasadchy.xtra.api.MiscApi
import com.github.andreyasadchy.xtra.db.RecentEmotesDao
import com.github.andreyasadchy.xtra.model.chat.BttvChannelResponse
import com.github.andreyasadchy.xtra.model.chat.BttvFfzResponse
import com.github.andreyasadchy.xtra.model.chat.BttvGlobalResponse
import com.github.andreyasadchy.xtra.model.chat.RecentEmote
import com.github.andreyasadchy.xtra.model.chat.RecentMessagesResponse
import com.github.andreyasadchy.xtra.model.chat.StvEmotesResponse
import com.github.andreyasadchy.xtra.model.chat.TwitchBadgesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(
    private val misc: MiscApi,
    private val recentEmotes: RecentEmotesDao
) {
    suspend fun loadRecentMessages(channelLogin: String, limit: String): Response<RecentMessagesResponse> = withContext(Dispatchers.IO) {
        misc.getRecentMessages(channelLogin, limit)
    }

    suspend fun loadGlobalBadges(): Response<TwitchBadgesResponse> = withContext(Dispatchers.IO) {
        misc.getGlobalBadges()
    }

    suspend fun loadChannelBadges(channelId: String): Response<TwitchBadgesResponse> = withContext(Dispatchers.IO) {
        misc.getChannelBadges(channelId)
    }

    suspend fun loadGlobalStvEmotes(): Response<StvEmotesResponse> = withContext(Dispatchers.IO) {
        misc.getGlobalStvEmotes()
    }

    suspend fun loadGlobalBttvEmotes(): Response<BttvGlobalResponse> = withContext(Dispatchers.IO) {
        misc.getGlobalBttvEmotes()
    }

    suspend fun loadBttvGlobalFfzEmotes(): Response<BttvFfzResponse> = withContext(Dispatchers.IO) {
        misc.getBttvGlobalFfzEmotes()
    }

    suspend fun loadStvEmotes(channelId: String): Response<StvEmotesResponse> = withContext(Dispatchers.IO) {
        misc.getStvEmotes(channelId)
    }

    suspend fun loadBttvEmotes(channelId: String): Response<BttvChannelResponse> = withContext(Dispatchers.IO) {
        misc.getBttvEmotes(channelId)
    }

    suspend fun loadBttvFfzEmotes(channelId: String): Response<BttvFfzResponse> = withContext(Dispatchers.IO) {
        misc.getBttvFfzEmotes(channelId)
    }

    fun loadRecentEmotes() = recentEmotes.getAll()

    fun insertRecentEmotes(emotes: Collection<RecentEmote>) {
        GlobalScope.launch {
            val listSize = emotes.size
            val list = if (listSize <= RecentEmote.MAX_SIZE) {
                emotes
            } else {
                emotes.toList().subList(listSize - RecentEmote.MAX_SIZE, listSize)
            }
            recentEmotes.ensureMaxSizeAndInsert(list)
        }
    }
}
