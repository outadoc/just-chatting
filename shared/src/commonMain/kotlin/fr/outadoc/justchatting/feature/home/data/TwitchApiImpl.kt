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
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

internal class TwitchApiImpl(
    private val twitchClient: TwitchClient,
) : TwitchApi {

    override suspend fun getStreamsByUserId(ids: List<String>): Result<List<Stream>> {
        return twitchClient
            .getStreamsByUserId(ids)
            .map { response ->
                response.data.map { stream ->
                    Stream(
                        id = stream.id,
                        userId = stream.userId,
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
                        userId = stream.userId,
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

    override suspend fun getUsersById(ids: List<String>): List<User> {
        if (ids.isEmpty()) return emptyList()
        return ids
            .chunked(MAX_PAGE_SIZE)
            .flatMap { chunkOfIds ->
                twitchClient
                    .getUsersById(chunkOfIds)
                    .fold(
                        onSuccess = { response ->
                            response.data.map { user ->
                                User(
                                    id = user.id,
                                    login = user.login,
                                    displayName = user.displayName,
                                    description = user.description,
                                    profileImageUrl = user.profileImageUrl.orEmpty(),
                                    createdAt = Instant.parse(user.createdAt),
                                    usedAt = null,
                                )
                            }
                        },
                        onFailure = { exception ->
                            logError<TwitchApiImpl>(exception) {
                                "Error while fetching users by ID: bad chunk"
                            }
                            emptyList()
                        },
                    )
            }
            .also { users ->
                if (users.size != ids.size) {
                    logError<TwitchApiImpl> { "Error while fetching users by ID: missing users" }
                }
            }
    }

    override suspend fun getUsersByLogin(logins: List<String>): List<User> {
        if (logins.isEmpty()) return emptyList()
        return logins
            .chunked(MAX_PAGE_SIZE)
            .flatMap { chunkOfLogins ->
                twitchClient
                    .getUsersById(chunkOfLogins)
                    .fold(
                        onSuccess = { response ->
                            response.data.map { user ->
                                User(
                                    id = user.id,
                                    login = user.login,
                                    displayName = user.displayName,
                                    description = user.description,
                                    profileImageUrl = user.profileImageUrl.orEmpty(),
                                    createdAt = Instant.parse(user.createdAt),
                                    usedAt = null,
                                )
                            }
                        },
                        onFailure = { exception ->
                            logError<TwitchApiImpl>(exception) {
                                "Error while fetching users by login: bad chunk"
                            }
                            emptyList()
                        },
                    )
            }
            .also { users ->
                if (users.size != logins.size) {
                    logError<TwitchApiImpl> { "Error while fetching users by login: missing users" }
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

    override suspend fun getFollowedChannels(userId: String): Result<List<ChannelFollow>> =
        runCatching {
            buildList {
                var cursor: String? = null
                do {
                    twitchClient
                        .getFollowedChannels(
                            userId = userId,
                            limit = MAX_PAGE_SIZE,
                            after = cursor,
                        )
                        .onSuccess { response ->
                            logDebug<TwitchApiImpl> {
                                "getFollowedChannels: loaded ${response.data.size} more items"
                            }

                            cursor = response.pagination.cursor

                            addAll(
                                response.data.map { follow ->
                                    ChannelFollow(
                                        user = User(
                                            id = follow.userId,
                                            login = follow.userLogin,
                                            displayName = follow.userDisplayName,
                                            description = "",
                                            profileImageUrl = "",
                                            createdAt = Instant.DISTANT_PAST,
                                            usedAt = Instant.DISTANT_PAST,
                                        ),
                                        followedAt = Instant.parse(follow.followedAt),
                                    )
                                },
                            )
                        }
                } while (cursor != null)
            }
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

    private companion object {
        const val MAX_PAGE_SIZE = 100
    }
}
