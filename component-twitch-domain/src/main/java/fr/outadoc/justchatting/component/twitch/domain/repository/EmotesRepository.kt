package fr.outadoc.justchatting.component.twitch.domain.repository

import fr.outadoc.justchatting.component.twitch.domain.model.BttvEmote
import fr.outadoc.justchatting.component.twitch.domain.model.FfzEmote
import fr.outadoc.justchatting.component.twitch.domain.model.RecentEmote
import fr.outadoc.justchatting.component.twitch.domain.model.StvEmote
import fr.outadoc.justchatting.component.twitch.domain.model.TwitchBadge
import fr.outadoc.justchatting.component.chatapi.db.MaxRecentEmotes
import fr.outadoc.justchatting.component.chatapi.db.RecentEmotesDao
import fr.outadoc.justchatting.component.twitch.api.BttvEmotesApi
import fr.outadoc.justchatting.component.twitch.api.StvEmotesApi
import fr.outadoc.justchatting.component.twitch.api.TwitchBadgesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class EmotesRepository(
    private val twitchBadgesApi: TwitchBadgesApi,
    private val stvEmotesApi: StvEmotesApi,
    private val bttvEmotesApi: BttvEmotesApi,
    private val recentEmotes: RecentEmotesDao
) {
    suspend fun loadGlobalBadges(): List<TwitchBadge> =
        withContext(Dispatchers.IO) {
            twitchBadgesApi.getGlobalBadges()
        }

    suspend fun loadChannelBadges(channelId: String): List<TwitchBadge> =
        withContext(Dispatchers.IO) {
            twitchBadgesApi.getChannelBadges(channelId)
        }

    suspend fun loadGlobalStvEmotes(): List<StvEmote> =
        withContext(Dispatchers.IO) {
            stvEmotesApi.getGlobalStvEmotes()
        }

    suspend fun loadStvEmotes(channelId: String): List<StvEmote> =
        withContext(Dispatchers.IO) {
            stvEmotesApi.getStvEmotes(channelId)
        }

    suspend fun loadGlobalBttvEmotes(): List<fr.outadoc.justchatting.component.twitch.domain.model.BttvEmote> =
        withContext(Dispatchers.IO) {
            bttvEmotesApi.getGlobalBttvEmotes()
        }

    suspend fun loadBttvGlobalFfzEmotes(): List<FfzEmote> =
        withContext(Dispatchers.IO) {
            bttvEmotesApi.getBttvGlobalFfzEmotes()
        }

    suspend fun loadBttvEmotes(channelId: String): List<fr.outadoc.justchatting.component.twitch.domain.model.BttvEmote> =
        withContext(Dispatchers.IO) {
            bttvEmotesApi.getBttvEmotes(channelId)
        }

    suspend fun loadBttvFfzEmotes(channelId: String): List<FfzEmote> =
        withContext(Dispatchers.IO) {
            bttvEmotesApi.getBttvFfzEmotes(channelId)
        }

    fun loadRecentEmotes(): Flow<List<RecentEmote>> = recentEmotes.getAll()

    suspend fun insertRecentEmotes(emotes: Collection<RecentEmote>) =
        withContext(Dispatchers.IO) {
            val listSize = emotes.size
            val list = if (listSize <= MaxRecentEmotes) {
                emotes
            } else {
                emotes.toList().subList(listSize - MaxRecentEmotes, listSize)
            }
            recentEmotes.ensureMaxSizeAndInsert(list)
        }
}
