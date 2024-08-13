package fr.outadoc.justchatting.feature.home.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.feature.home.domain.TwitchApi
import fr.outadoc.justchatting.feature.home.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleForDay
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.home.domain.model.User
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

internal class TwitchApiImpl(
    private val twitchClient: TwitchClient,
    private val clock: Clock,
) : TwitchApi {

    override suspend fun getStreamsByUserId(ids: List<String>): Result<List<Stream>> {
        return twitchClient
            .getStreamsByUserId(ids)
            .map { response ->
                response.data.map { stream ->
                    Stream(
                        id = stream.id,
                        user = User(
                            id = stream.userId,
                            login = stream.userLogin,
                            displayName = stream.userName,
                            usedAt = channel.used_at?.let { Instant.fromEpochMilliseconds(it) },

                        ),
                        gameName = stream.gameName,
                        title = stream.title,
                        viewerCount = stream.viewerCount,
                        startedAt = stream.startedAt,
                        tags = stream.tags.toPersistentList(),
                    )
                }
            }
    }

    override suspend fun getStreamsByUserLogin(logins: List<String>): Result<List<Stream>> {
        return twitchClient
            .getStreamsByUserLogin(logins)
            .map { response ->
                response.data.map { stream ->
                    Stream(
                        id = stream.id,
                        user = User(
                            id = stream.userId,
                            login = stream.userLogin,
                            displayName = stream.userName,
                            usedAt = channel.used_at?.let { Instant.fromEpochMilliseconds(it) },

                        ),
                        gameName = stream.gameName,
                        title = stream.title,
                        viewerCount = stream.viewerCount,
                        startedAt = stream.startedAt,
                        tags = stream.tags.toPersistentList(),
                    )
                }
            }
    }

    override suspend fun getFollowedStreams(userId: String): Flow<PagingData<List<Stream>>> {
        val pager = Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                prefetchDistance = 10,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                FollowedStreamsDataSource(
                    userId = userId,
                    twitchClient = twitchClient,
                )
            },
        )

        return pager.flow
    }

    override suspend fun getUsersById(ids: List<String>): Result<List<User>> {
        return twitchClient
            .getUsersById(ids)
            .map { response ->
                response.data.map { user ->
                    User(
                        id = user.id,
                        login = user.login,
                        displayName = user.displayName,
                        description = user.description,
                        profileImageUrl = user.profileImageUrl,
                        createdAt = user.createdAt,
                        usedAt = channel.used_at?.let { Instant.fromEpochMilliseconds(it) },
                    )
                }
            }
    }

    override suspend fun getUsersByLogin(logins: List<String>): Result<List<User>> {
        return twitchClient
            .getUsersByLogin(logins)
            .mapCatching { response ->
                if (response.data.isEmpty()) {
                    error("No users found for logins: $logins")
                }

                response.data.map { user ->
                    User(
                        id = user.id,
                        login = user.login,
                        displayName = user.displayName,
                        description = user.description,
                        profileImageUrl = user.profileImageUrl,
                        createdAt = user.createdAt,
                        usedAt = channel.used_at?.let { Instant.fromEpochMilliseconds(it) },
                    )
                }
            }
    }

    override suspend fun searchChannels(
        query: String,
    ): Flow<PagingData<List<ChannelSearchResult>>> {
        val pager = Pager(
            config = PagingConfig(
                pageSize = 15,
                initialLoadSize = 15,
                prefetchDistance = 5,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                SearchChannelsDataSource(
                    query = query,
                    twitchClient = twitchClient,
                )
            },
        )

        return pager.flow
    }

    override suspend fun getFollowedChannels(userId: String): Flow<PagingData<List<ChannelFollow>>> {
        val pager = Pager(
            config = PagingConfig(
                pageSize = 40,
                initialLoadSize = 40,
                prefetchDistance = 10,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                FollowedChannelsDataSource(
                    userId = userId,
                    twitchClient = twitchClient,
                )
            },
        )

        return pager.flow
    }

    override suspend fun getEmotesFromSet(setIds: List<String>): Result<List<Emote>> {
        return twitchClient
            .getEmotesFromSet(setIds)
            .map { response ->
                response.data
                    .sortedByDescending { it.setId }
                    .map { emote -> emote.map(templateUrl = response.template) }
            }
    }

    override suspend fun getCheerEmotes(userId: String?): Result<List<Emote>> {
        return twitchClient
            .getCheerEmotes(userId)
            .map { response ->
                response.data.flatMap { emote ->
                    emote.tiers.map { tier ->
                        tier.map(prefix = emote.prefix)
                    }
                }
            }
    }

    override suspend fun getGlobalBadges(): Result<List<TwitchBadge>> {
        return twitchClient
            .getGlobalBadges()
            .map { result ->
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

    override suspend fun getChannelBadges(channelId: String): Result<List<TwitchBadge>> {
        return twitchClient
            .getChannelBadges(channelId)
            .map { result ->
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

    override suspend fun getChannelSchedule(
        channelId: String,
        start: Instant,
        pastRange: DatePeriod,
        futureRange: DatePeriod,
        timeZone: TimeZone,
    ): Flow<PagingData<ChannelScheduleForDay>> {
        val pager = Pager(
            config = PagingConfig(
                pageSize = 15,
                initialLoadSize = 25,
                prefetchDistance = 10,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                ChannelScheduleDataSource(
                    channelId = channelId,
                    twitchClient = twitchClient,
                    start = start,
                    pastRange = pastRange,
                    futureRange = futureRange,
                    timeZone = timeZone,
                )
            },
        )

        return pager.flow
    }
}
