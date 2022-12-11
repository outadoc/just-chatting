package fr.outadoc.justchatting.repository

import fr.outadoc.justchatting.api.BttvEmotesApi
import fr.outadoc.justchatting.api.StvEmotesApi
import fr.outadoc.justchatting.api.TwitchBadgesApi
import fr.outadoc.justchatting.component.twitch.model.BttvChannelResponse
import fr.outadoc.justchatting.component.twitch.model.BttvFfzResponse
import fr.outadoc.justchatting.component.twitch.model.BttvGlobalResponse
import fr.outadoc.justchatting.component.twitch.model.RecentEmote
import fr.outadoc.justchatting.component.twitch.model.StvEmotesResponse
import fr.outadoc.justchatting.component.twitch.model.TwitchBadgesResponse
import fr.outadoc.justchatting.db.RecentEmotesDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class EmotesRepository(
    private val twitchBadgesApi: TwitchBadgesApi,
    private val stvEmotesApi: StvEmotesApi,
    private val bttvEmotesApi: BttvEmotesApi,
    private val recentEmotes: RecentEmotesDao
) {
    suspend fun loadGlobalBadges(): Response<TwitchBadgesResponse> =
        withContext(Dispatchers.IO) {
            twitchBadgesApi.getGlobalBadges()
        }

    suspend fun loadChannelBadges(channelId: String): Response<TwitchBadgesResponse> =
        withContext(Dispatchers.IO) {
            twitchBadgesApi.getChannelBadges(channelId)
        }

    suspend fun loadGlobalStvEmotes(): Response<StvEmotesResponse> =
        withContext(Dispatchers.IO) {
            stvEmotesApi.getGlobalStvEmotes()
        }

    suspend fun loadStvEmotes(channelId: String): Response<StvEmotesResponse> =
        withContext(Dispatchers.IO) {
            stvEmotesApi.getStvEmotes(channelId)
        }

    suspend fun loadGlobalBttvEmotes(): Response<BttvGlobalResponse> =
        withContext(Dispatchers.IO) {
            bttvEmotesApi.getGlobalBttvEmotes()
        }

    suspend fun loadBttvGlobalFfzEmotes(): Response<BttvFfzResponse> =
        withContext(Dispatchers.IO) {
            bttvEmotesApi.getBttvGlobalFfzEmotes()
        }

    suspend fun loadBttvEmotes(channelId: String): Response<BttvChannelResponse> =
        withContext(Dispatchers.IO) {
            bttvEmotesApi.getBttvEmotes(channelId)
        }

    suspend fun loadBttvFfzEmotes(channelId: String): Response<BttvFfzResponse> =
        withContext(Dispatchers.IO) {
            bttvEmotesApi.getBttvFfzEmotes(channelId)
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
