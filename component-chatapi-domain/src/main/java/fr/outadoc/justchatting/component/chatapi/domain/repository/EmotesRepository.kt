package fr.outadoc.justchatting.component.chatapi.domain.repository

import fr.outadoc.justchatting.component.chatapi.db.MaxRecentEmotes
import fr.outadoc.justchatting.component.chatapi.db.RecentEmotesDao
import fr.outadoc.justchatting.component.chatapi.domain.model.BttvEmote
import fr.outadoc.justchatting.component.chatapi.domain.model.FfzEmote
import fr.outadoc.justchatting.component.chatapi.domain.model.RecentEmote
import fr.outadoc.justchatting.component.chatapi.domain.model.StvEmote
import fr.outadoc.justchatting.component.chatapi.domain.model.TwitchBadge
import fr.outadoc.justchatting.component.twitch.api.BttvEmotesApi
import fr.outadoc.justchatting.component.twitch.api.StvEmotesApi
import fr.outadoc.justchatting.component.twitch.api.TwitchBadgesApi
import fr.outadoc.justchatting.component.twitch.model.isZeroWidth
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
                .badgeSets
                .flatMap { (setName, set) ->
                    set.versions.map { (versionName, version) ->
                        TwitchBadge(
                            id = setName,
                            version = versionName,
                            urls = persistentMapOf(
                                1f to version.image1x,
                                2f to version.image2x,
                                4f to version.image4x
                            )
                        )
                    }
                }
        }

    suspend fun loadChannelBadges(channelId: String): List<TwitchBadge> =
        withContext(Dispatchers.IO) {
            twitchBadgesApi.getChannelBadges(channelId)
                .badgeSets
                .flatMap { (setName, set) ->
                    set.versions.map { (versionName, version) ->
                        TwitchBadge(
                            id = setName,
                            version = versionName,
                            urls = persistentMapOf(
                                1f to version.image1x,
                                2f to version.image2x,
                                4f to version.image4x
                            )
                        )
                    }
                }
        }

    suspend fun loadGlobalStvEmotes(): List<StvEmote> =
        withContext(Dispatchers.IO) {
            stvEmotesApi.getGlobalStvEmotes().map { emote ->
                StvEmote(
                    name = emote.name,
                    isZeroWidth = emote.isZeroWidth,
                    urls = emote.urls.associate { (density, url) ->
                        density.toFloat() to url
                    }
                )
            }
        }

    suspend fun loadStvEmotes(channelId: String): List<StvEmote> =
        withContext(Dispatchers.IO) {
            stvEmotesApi.getStvEmotes(channelId).map { emote ->
                StvEmote(
                    name = emote.name,
                    isZeroWidth = emote.isZeroWidth,
                    urls = emote.urls.associate { (density, url) ->
                        density.toFloat() to url
                    }
                )
            }
        }

    suspend fun loadGlobalBttvEmotes(): List<BttvEmote> =
        withContext(Dispatchers.IO) {
            bttvEmotesApi.getGlobalBttvEmotes().map { emote ->
                BttvEmote(
                    id = emote.id,
                    name = emote.code
                )
            }
        }

    suspend fun loadBttvGlobalFfzEmotes(): List<FfzEmote> =
        withContext(Dispatchers.IO) {
            bttvEmotesApi.getBttvGlobalFfzEmotes().map { emote ->
                FfzEmote(
                    name = emote.code,
                    urls = emote.images
                )
            }
        }

    suspend fun loadBttvEmotes(channelId: String): List<BttvEmote> =
        withContext(Dispatchers.IO) {
            bttvEmotesApi.getBttvEmotes(channelId).allEmotes.map { emote ->
                BttvEmote(
                    id = emote.id,
                    name = emote.code
                )
            }
        }

    suspend fun loadBttvFfzEmotes(channelId: String): List<FfzEmote> =
        withContext(Dispatchers.IO) {
            bttvEmotesApi.getBttvFfzEmotes(channelId).map { emote ->
                FfzEmote(
                    name = emote.code,
                    urls = emote.images
                )
            }
        }

    fun loadRecentEmotes(): Flow<List<RecentEmote>> =
        recentEmotes.getAll().map { emotes ->
            emotes.map { emote ->
                RecentEmote(
                    name = emote.name,
                    url = emote.url,
                    usedAt = emote.usedAt
                )
            }
        }

    suspend fun insertRecentEmotes(emotes: Collection<RecentEmote>) =
        withContext(Dispatchers.IO) {
            val listSize: Int = emotes.size
            val list: Collection<RecentEmote> =
                if (listSize <= MaxRecentEmotes) emotes
                else emotes.toList().subList(listSize - MaxRecentEmotes, listSize)

            recentEmotes.ensureMaxSizeAndInsert(
                list.map { emote ->
                    fr.outadoc.justchatting.component.chatapi.db.RecentEmote(
                        name = emote.name,
                        url = emote.url,
                        usedAt = emote.usedAt
                    )
                }
            )
        }
}
