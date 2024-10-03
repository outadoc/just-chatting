package fr.outadoc.justchatting.feature.shared.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import fr.outadoc.justchatting.feature.chat.data.http.map
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.emotes.data.twitch.map
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.feature.followed.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.search.data.SearchChannelsDataSource
import fr.outadoc.justchatting.feature.search.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.shared.domain.TwitchApi
import fr.outadoc.justchatting.feature.shared.domain.model.MessageNotSentException
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.timeline.domain.model.Video
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

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
                        category = if (stream.gameId != null && stream.gameName != null) {
                            StreamCategory(
                                id = stream.gameId,
                                name = stream.gameName,
                            )
                        } else {
                            null
                        },
                        title = stream.title,
                        viewerCount = stream.viewerCount,
                        startedAt = Instant.parse(stream.startedAt),
                        tags = stream.tags.toPersistentSet(),
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
                        category = if (stream.gameId != null && stream.gameName != null) {
                            StreamCategory(
                                id = stream.gameId,
                                name = stream.gameName,
                            )
                        } else {
                            null
                        },
                        title = stream.title,
                        viewerCount = stream.viewerCount,
                        startedAt = Instant.parse(stream.startedAt),
                        tags = stream.tags.toPersistentSet(),
                    )
                }
            }
    }

    override suspend fun getFollowedStreams(userId: String): Result<List<Stream>> =
        runCatching {
            buildList {
                var cursor: String? = null
                do {
                    twitchClient
                        .getFollowedStreams(
                            userId = userId,
                            limit = MAX_PAGE_SIZE_DEFAULT,
                            after = cursor,
                        )
                        .onFailure { exception ->
                            logError<TwitchApiImpl>(exception) {
                                "getFollowedStreams: failed to load more items"
                            }
                            // TODO fail everything if one request fails
                        }
                        .onSuccess { response ->
                            logDebug<TwitchApiImpl> {
                                "getFollowedStreams: loaded ${response.data.size} more items"
                            }

                            cursor = response.pagination.cursor

                            addAll(
                                response.data.map { stream ->
                                    Stream(
                                        id = stream.id,
                                        userId = stream.userId,
                                        category = if (stream.gameId != null && stream.gameName != null) {
                                            StreamCategory(
                                                id = stream.gameId,
                                                name = stream.gameName,
                                            )
                                        } else {
                                            null
                                        },
                                        title = stream.title,
                                        viewerCount = stream.viewerCount,
                                        startedAt = Instant.parse(stream.startedAt),
                                        tags = stream.tags.toPersistentSet(),
                                    )
                                },
                            )
                        }
                } while (cursor != null)
            }
        }

    override suspend fun getUsersById(ids: List<String>): List<User> {
        if (ids.isEmpty()) return emptyList()
        return ids
            .chunked(MAX_PAGE_SIZE_DEFAULT)
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
            .chunked(MAX_PAGE_SIZE_DEFAULT)
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
                            limit = MAX_PAGE_SIZE_DEFAULT,
                            after = cursor,
                        )
                        .onFailure { exception ->
                            logError<TwitchApiImpl>(exception) {
                                "getFollowedChannels: failed to load more items"
                            }
                        }
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

    override suspend fun getChannelVideos(
        channelId: String,
        notBefore: Instant,
    ): Result<List<Video>> =
        runCatching {
            buildList {
                var cursor: String? = null
                var currentMinInstant: Instant? = null

                do {
                    twitchClient
                        .getChannelVideos(
                            userId = channelId,
                            limit = MAX_PAGE_SIZE_DEFAULT,
                            after = cursor,
                        )
                        .onFailure { exception ->
                            logError<TwitchApiImpl>(exception) {
                                "getChannelVideos: failed to load more items"
                            }
                        }
                        .onSuccess { response ->
                            logDebug<TwitchApiImpl> {
                                "getChannelVideos: loaded ${response.data.size} more items"
                            }

                            val mappedVideos = response.data.map { video ->
                                Video(
                                    id = video.id,
                                    title = video.title,
                                    thumbnailUrl = video.thumbnailUrl,
                                    publishedAt = Instant.parse(video.publishedAtIso),
                                    duration = video.duration.parseTwitchDuration(),
                                    streamId = video.streamId,
                                    viewCount = video.viewCount,
                                    videoUrl = video.videoUrl,
                                    userId = video.userId,
                                    createdAt = Instant.parse(video.createdAtIso),
                                    description = video.description,
                                )
                            }

                            cursor = response.pagination.cursor
                            currentMinInstant = mappedVideos.minOfOrNull { it.createdAt }

                            addAll(mappedVideos)
                        }
                } while (
                    cursor != null &&
                    ((currentMinInstant ?: Instant.DISTANT_FUTURE) > notBefore)
                )
            }
        }

    override suspend fun getChannelSchedule(
        userId: String,
        notBefore: Instant,
        notAfter: Instant,
    ): Result<List<ChannelScheduleSegment>> =
        runCatching {
            buildList {
                var cursor: String? = null
                var currentMaxInstant: Instant? = null

                do {
                    twitchClient
                        .getChannelSchedule(
                            userId = userId,
                            limit = MAX_PAGE_SIZE_GET_SCHEDULE,
                            start = notBefore,
                            after = cursor,
                        )
                        .onFailure { exception ->
                            logError<TwitchApiImpl>(exception) {
                                "getChannelSchedule: failed to load more items"
                            }
                        }
                        .onSuccess { response ->
                            val segments = response.data.segments.orEmpty()

                            logDebug<TwitchApiImpl> {
                                "getChannelSchedule: loaded ${segments.size} more items"
                            }

                            val mappedSegments = segments.map { segment ->
                                ChannelScheduleSegment(
                                    id = segment.id,
                                    user = User(
                                        id = userId,
                                        login = "",
                                        displayName = "",
                                        description = "",
                                        profileImageUrl = "",
                                        createdAt = Instant.DISTANT_PAST,
                                        usedAt = Instant.DISTANT_PAST,
                                    ),
                                    title = segment.title,
                                    startTime = Instant.parse(segment.startTimeIso),
                                    endTime = segment.endTimeIso?.let { Instant.parse(it) },
                                    category = segment.category?.let { category ->
                                        StreamCategory(
                                            id = category.id,
                                            name = category.name,
                                        )
                                    },
                                )
                            }

                            addAll(mappedSegments)

                            cursor = response.pagination.cursor
                            currentMaxInstant = mappedSegments.maxOfOrNull { it.startTime }
                        }
                } while (
                    cursor != null &&
                    ((currentMaxInstant ?: Instant.DISTANT_PAST) < notAfter)
                )
            }
        }

    override suspend fun sendChatMessage(
        channelUserId: String,
        senderUserId: String,
        message: String,
        inReplyToMessageId: String?,
    ): Result<String> {
        return twitchClient
            .sendChatMessage(
                channelUserId = channelUserId,
                senderUserId = senderUserId,
                message = message,
                inReplyToMessageId = inReplyToMessageId,
            )
            .map { response -> response.data.firstOrNull() }
            .mapCatching { response ->
                if (response == null) {
                    throw MessageNotSentException("Response was empty")
                }

                if (response.dropReason != null) {
                    throw MessageNotSentException(
                        "Message was not sent: ${response.dropReason.message} (${response.dropReason.code})",
                        dropReasonCode = response.dropReason.code,
                        dropReasonMessage = response.dropReason.message,
                    )
                }

                if (!response.isSent) {
                    throw MessageNotSentException("Message was not sent")
                }

                response.messageId
            }
    }

    private companion object {
        const val MAX_PAGE_SIZE_DEFAULT = 100
        const val MAX_PAGE_SIZE_GET_SCHEDULE = 25
    }
}
