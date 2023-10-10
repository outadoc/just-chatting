package fr.outadoc.justchatting.component.chatapi.domain.repository

import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.chatapi.common.EmoteUrls
import fr.outadoc.justchatting.component.chatapi.db.RecentEmotesRepository
import fr.outadoc.justchatting.component.chatapi.db.Recent_emotes
import fr.outadoc.justchatting.component.chatapi.domain.model.RecentEmote
import fr.outadoc.justchatting.component.chatapi.domain.model.TwitchBadge
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.component.twitch.http.api.BttvEmotesApi
import fr.outadoc.justchatting.component.twitch.http.api.HelixApi
import fr.outadoc.justchatting.component.twitch.http.api.StvEmotesApi
import fr.outadoc.justchatting.component.twitch.utils.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class EmotesRepository(
    private val helixApi: HelixApi,
    private val stvEmotesApi: StvEmotesApi,
    private val bttvEmotesApi: BttvEmotesApi,
    private val recentEmotes: RecentEmotesRepository,
    private val preferencesRepository: PreferenceRepository,
) {
    suspend fun loadGlobalBadges(): List<TwitchBadge> =
        withContext(Dispatchers.IO) {
            helixApi.getGlobalBadges()
                .badgeSets
                .flatMap { set ->
                    set.versions.map { version ->
                        TwitchBadge(
                            setId = set.setId,
                            version = version.id,
                            urls = EmoteUrls(
                                mapOf(
                                    1f to version.image1x,
                                    2f to version.image2x,
                                    4f to version.image4x,
                                ),
                            ),
                        )
                    }
                }
        }

    suspend fun loadChannelBadges(channelId: String): List<TwitchBadge> =
        withContext(Dispatchers.IO) {
            helixApi.getChannelBadges(channelId)
                .badgeSets
                .flatMap { set ->
                    set.versions.map { version ->
                        TwitchBadge(
                            setId = set.setId,
                            version = version.id,
                            urls = EmoteUrls(
                                mapOf(
                                    1f to version.image1x,
                                    2f to version.image2x,
                                    4f to version.image4x,
                                ),
                            ),
                        )
                    }
                }
        }

    suspend fun loadGlobalStvEmotes(): List<Emote> =
        withContext(Dispatchers.IO) {
            if (!preferencesRepository.currentPreferences.first().enableStvEmotes) {
                return@withContext emptyList()
            }

            stvEmotesApi.getGlobalStvEmotes()
                .map { emote -> emote.map() }
        }

    suspend fun loadStvEmotes(channelId: String): List<Emote> =
        withContext(Dispatchers.IO) {
            if (!preferencesRepository.currentPreferences.first().enableStvEmotes) {
                return@withContext emptyList()
            }

            stvEmotesApi.getStvEmotes(channelId)
                .map { emote -> emote.map() }
        }

    suspend fun loadGlobalBttvEmotes(): List<Emote> =
        withContext(Dispatchers.IO) {
            if (!preferencesRepository.currentPreferences.first().enableBttvEmotes) {
                return@withContext emptyList()
            }

            bttvEmotesApi.getGlobalBttvEmotes()
                .map { emote -> emote.map() }
        }

    suspend fun loadBttvEmotes(channelId: String): List<Emote> =
        withContext(Dispatchers.IO) {
            if (!preferencesRepository.currentPreferences.first().enableBttvEmotes) {
                return@withContext emptyList()
            }

            bttvEmotesApi.getBttvEmotes(channelId).allEmotes
                .map { emote -> emote.map() }
        }

    suspend fun loadBttvGlobalFfzEmotes(): List<Emote> =
        withContext(Dispatchers.IO) {
            if (!preferencesRepository.currentPreferences.first().enableFfzEmotes) {
                return@withContext emptyList()
            }

            bttvEmotesApi.getBttvGlobalFfzEmotes()
                .map { emote -> emote.map() }
        }

    suspend fun loadBttvFfzEmotes(channelId: String): List<Emote> =
        withContext(Dispatchers.IO) {
            if (!preferencesRepository.currentPreferences.first().enableFfzEmotes) {
                return@withContext emptyList()
            }

            bttvEmotesApi.getBttvFfzEmotes(channelId)
                .map { emote -> emote.map() }
        }

    fun loadRecentEmotes(): Flow<List<RecentEmote>> =
        recentEmotes.getAll().map { emotes ->
            emotes.map { emote ->
                RecentEmote(
                    name = emote.name,
                    url = emote.url,
                    usedAt = emote.used_at,
                )
            }
        }

    suspend fun insertRecentEmotes(emotes: Collection<RecentEmote>) =
        withContext(Dispatchers.IO) {
            recentEmotes.insertAll(
                emotes.map { emote ->
                    Recent_emotes(
                        name = emote.name,
                        url = emote.url,
                        used_at = emote.usedAt,
                    )
                },
            )
        }
}
