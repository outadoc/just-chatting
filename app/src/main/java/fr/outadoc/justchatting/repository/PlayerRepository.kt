package fr.outadoc.justchatting.repository

import fr.outadoc.justchatting.api.MiscApi
import fr.outadoc.justchatting.db.RecentEmotesDao
import fr.outadoc.justchatting.model.chat.BttvChannelResponse
import fr.outadoc.justchatting.model.chat.BttvFfzResponse
import fr.outadoc.justchatting.model.chat.BttvGlobalResponse
import fr.outadoc.justchatting.model.chat.RecentEmote
import fr.outadoc.justchatting.model.chat.RecentMessagesResponse
import fr.outadoc.justchatting.model.chat.StvEmotesResponse
import fr.outadoc.justchatting.model.chat.TwitchBadgesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(
    private val misc: MiscApi,
    private val recentEmotes: RecentEmotesDao
) {
    suspend fun loadRecentMessages(
        channelLogin: String,
        limit: Int
    ): Response<RecentMessagesResponse> =
        withContext(Dispatchers.IO) {
            misc.getRecentMessages(channelLogin, limit)
        }

    suspend fun loadGlobalBadges(): Response<TwitchBadgesResponse> =
        withContext(Dispatchers.IO) {
            misc.getGlobalBadges()
        }

    suspend fun loadChannelBadges(channelId: String): Response<TwitchBadgesResponse> =
        withContext(Dispatchers.IO) {
            misc.getChannelBadges(channelId)
        }

    suspend fun loadGlobalStvEmotes(): Response<StvEmotesResponse> =
        withContext(Dispatchers.IO) {
            misc.getGlobalStvEmotes()
        }

    suspend fun loadGlobalBttvEmotes(): Response<BttvGlobalResponse> =
        withContext(Dispatchers.IO) {
            misc.getGlobalBttvEmotes()
        }

    suspend fun loadBttvGlobalFfzEmotes(): Response<BttvFfzResponse> =
        withContext(Dispatchers.IO) {
            misc.getBttvGlobalFfzEmotes()
        }

    suspend fun loadStvEmotes(channelId: String): Response<StvEmotesResponse> =
        withContext(Dispatchers.IO) {
            misc.getStvEmotes(channelId)
        }

    suspend fun loadBttvEmotes(channelId: String): Response<BttvChannelResponse> =
        withContext(Dispatchers.IO) {
            misc.getBttvEmotes(channelId)
        }

    suspend fun loadBttvFfzEmotes(channelId: String): Response<BttvFfzResponse> =
        withContext(Dispatchers.IO) {
            misc.getBttvFfzEmotes(channelId)
        }

    fun loadRecentEmotes() = recentEmotes.getAll()

    suspend fun insertRecentEmotes(emotes: Collection<RecentEmote>) =
        withContext(Dispatchers.IO) {
            val listSize = emotes.size
            val list = if (listSize <= RecentEmote.MAX_SIZE) {
                emotes
            } else {
                emotes.toList().subList(listSize - RecentEmote.MAX_SIZE, listSize)
            }
            recentEmotes.ensureMaxSizeAndInsert(list)
        }
}
