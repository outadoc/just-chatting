package fr.outadoc.justchatting.feature.emotes.domain

import fr.outadoc.justchatting.db.Recent_emotes
import fr.outadoc.justchatting.feature.emotes.data.bttv.BttvEmotesApi
import fr.outadoc.justchatting.feature.emotes.data.bttv.model.map
import fr.outadoc.justchatting.feature.emotes.data.recent.RecentEmotesDao
import fr.outadoc.justchatting.feature.emotes.data.stv.StvEmotesApi
import fr.outadoc.justchatting.feature.emotes.data.stv.model.map
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.feature.emotes.domain.model.RecentEmote
import fr.outadoc.justchatting.feature.home.data.TwitchApi
import fr.outadoc.justchatting.feature.home.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

internal class EmotesRepository(
    private val twitchApi: TwitchApi,
    private val stvEmotesApi: StvEmotesApi,
    private val bttvEmotesApi: BttvEmotesApi,
    private val recentEmotes: RecentEmotesDao,
    private val preferencesRepository: PreferenceRepository,
) {
    suspend fun loadGlobalBadges(): Result<List<TwitchBadge>> =
        withContext(DispatchersProvider.io) {
            twitchApi.getGlobalBadges().map { result ->
                result.badgeSets.flatMap { set ->
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
        }

    suspend fun loadChannelBadges(channelId: String): Result<List<TwitchBadge>> =
        withContext(DispatchersProvider.io) {
            twitchApi.getChannelBadges(channelId).map { result ->
                result.badgeSets.flatMap { set ->
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
        }

    suspend fun loadGlobalStvEmotes(): Result<List<Emote>> =
        withContext(DispatchersProvider.io) {
            if (!preferencesRepository.currentPreferences.first().enableStvEmotes) {
                return@withContext Result.success(emptyList())
            }

            stvEmotesApi.getGlobalStvEmotes()
                .map { response ->
                    response.emotes
                        .map { emote -> emote.map() }
                }
        }

    suspend fun loadGlobalBttvEmotes(): Result<List<Emote>> =
        withContext(DispatchersProvider.io) {
            if (!preferencesRepository.currentPreferences.first().enableBttvEmotes) {
                return@withContext Result.success(emptyList())
            }

            bttvEmotesApi.getGlobalBttvEmotes()
                .map { response ->
                    response.map { emote -> emote.map() }
                }
        }

    suspend fun loadBttvEmotes(channelId: String): Result<List<Emote>> =
        withContext(DispatchersProvider.io) {
            if (!preferencesRepository.currentPreferences.first().enableBttvEmotes) {
                return@withContext Result.success(emptyList())
            }

            bttvEmotesApi.getBttvEmotes(channelId)
                .map { response ->
                    response.allEmotes
                        .map { emote -> emote.map() }
                }
        }

    suspend fun loadBttvGlobalFfzEmotes(): Result<List<Emote>> =
        withContext(DispatchersProvider.io) {
            if (!preferencesRepository.currentPreferences.first().enableFfzEmotes) {
                return@withContext Result.success(emptyList())
            }

            bttvEmotesApi.getBttvGlobalFfzEmotes()
                .map { response ->
                    response.map { emote -> emote.map() }
                }
        }

    suspend fun loadBttvFfzEmotes(channelId: String): Result<List<Emote>> =
        withContext(DispatchersProvider.io) {
            if (!preferencesRepository.currentPreferences.first().enableFfzEmotes) {
                return@withContext Result.success(emptyList())
            }

            bttvEmotesApi.getBttvFfzEmotes(channelId)
                .map { response ->
                    response.map { emote -> emote.map() }
                }
        }

    fun loadRecentEmotes(): Flow<List<RecentEmote>> =
        recentEmotes.getAll().map { emotes ->
            emotes.map { emote ->
                RecentEmote(
                    name = emote.name,
                    url = emote.url,
                    usedAt = Instant.fromEpochMilliseconds(emote.used_at),
                )
            }
        }

    suspend fun insertRecentEmotes(emotes: Collection<RecentEmote>) =
        withContext(DispatchersProvider.io) {
            recentEmotes.insertAll(
                emotes.map { emote ->
                    Recent_emotes(
                        name = emote.name,
                        url = emote.url,
                        used_at = emote.usedAt.toEpochMilliseconds(),
                    )
                },
            )
        }
}
