package fr.outadoc.justchatting.component.twitch.domain.repository

import fr.outadoc.justchatting.component.twitch.api.BttvEmotesApi
import fr.outadoc.justchatting.component.twitch.api.StvEmotesApi
import fr.outadoc.justchatting.component.twitch.api.TwitchBadgesApi
import fr.outadoc.justchatting.component.chatapi.db.RecentEmotesDao
import fr.outadoc.justchatting.component.twitch.model.BttvChannelResponse
import fr.outadoc.justchatting.component.twitch.model.BttvFfzResponse
import fr.outadoc.justchatting.component.twitch.model.BttvGlobalResponse
import fr.outadoc.justchatting.component.twitch.model.RecentEmote
import fr.outadoc.justchatting.component.twitch.model.StvEmotesResponse
import fr.outadoc.justchatting.component.twitch.model.TwitchBadgesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmotesRepository(
    private val twitchBadgesApi: TwitchBadgesApi,
    private val stvEmotesApi: StvEmotesApi,
    private val bttvEmotesApi: BttvEmotesApi,
    private val recentEmotes: RecentEmotesDao
) {
    suspend fun loadGlobalBadges(): TwitchBadgesResponse =
        withContext(Dispatchers.IO) {
            twitchBadgesApi.getGlobalBadges()
        }

    suspend fun loadChannelBadges(channelId: String): TwitchBadgesResponse =
        withContext(Dispatchers.IO) {
            twitchBadgesApi.getChannelBadges(channelId)
        }

    suspend fun loadGlobalStvEmotes(): StvEmotesResponse =
        withContext(Dispatchers.IO) {
            stvEmotesApi.getGlobalStvEmotes()
        }

    suspend fun loadStvEmotes(channelId: String): StvEmotesResponse =
        withContext(Dispatchers.IO) {
            stvEmotesApi.getStvEmotes(channelId)
        }

    suspend fun loadGlobalBttvEmotes(): BttvGlobalResponse =
        withContext(Dispatchers.IO) {
            bttvEmotesApi.getGlobalBttvEmotes()
        }

    suspend fun loadBttvGlobalFfzEmotes(): BttvFfzResponse =
        withContext(Dispatchers.IO) {
            bttvEmotesApi.getBttvGlobalFfzEmotes()
        }

    suspend fun loadBttvEmotes(channelId: String): BttvChannelResponse =
        withContext(Dispatchers.IO) {
            bttvEmotesApi.getBttvEmotes(channelId)
        }

    suspend fun loadBttvFfzEmotes(channelId: String): BttvFfzResponse =
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
