package fr.outadoc.justchatting.component.chatapi.domain.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.flatMap
import fr.outadoc.justchatting.component.chatapi.db.RecentChannelsDao
import fr.outadoc.justchatting.component.chatapi.db.Recent_channels
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelFollow
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSchedule
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelScheduleVacation
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import fr.outadoc.justchatting.component.chatapi.domain.repository.datasource.FollowedChannelsDataSource
import fr.outadoc.justchatting.component.chatapi.domain.repository.datasource.FollowedStreamsDataSource
import fr.outadoc.justchatting.component.chatapi.domain.repository.datasource.SearchChannelsDataSource
import fr.outadoc.justchatting.component.twitch.http.api.TwitchApi
import fr.outadoc.justchatting.feature.emotes.data.bttv.model.map
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.preferences.data.AppUser
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

internal class TwitchRepositoryImpl(
    private val twitchApi: TwitchApi,
    private val preferencesRepository: PreferenceRepository,
    private val recentChannelsDao: RecentChannelsDao,
) : TwitchRepository {

    override suspend fun loadSearchChannels(query: String): Flow<PagingData<ChannelSearchResult>> {
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
                    twitchApi = twitchApi,
                )
            },
        )

        return pager.flow.map { page ->
            page.flatMap { response ->
                val completeUsers: Map<String, User> =
                    response
                        .map { stream -> stream.user }
                        .mapWithProfileImages()
                        .associateBy { user -> user.id }

                response.map { stream ->
                    stream.copy(
                        user = completeUsers[stream.user.id] ?: stream.user,
                    )
                }
            }
        }
    }

    override suspend fun loadFollowedStreams(): Flow<PagingData<Stream>> {
        val appUser: AppUser = preferencesRepository.currentPreferences.first().appUser

        if (appUser !is AppUser.LoggedIn) return emptyFlow()

        val pager = Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                prefetchDistance = 10,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                FollowedStreamsDataSource(
                    userId = appUser.userId,
                    twitchApi = twitchApi,
                )
            },
        )

        return pager.flow.map { page ->
            page.flatMap { streams ->
                val completeUsers: Map<String, User> =
                    streams
                        .map { stream -> stream.user }
                        .mapWithProfileImages()
                        .associateBy { user -> user.id }

                streams.map { stream ->
                    stream.copy(
                        user = completeUsers[stream.user.id] ?: stream.user,
                    )
                }
            }
        }
    }

    override suspend fun loadFollowedChannels(): Flow<PagingData<ChannelFollow>> {
        val appUser: AppUser = preferencesRepository.currentPreferences.first().appUser

        if (appUser !is AppUser.LoggedIn) return emptyFlow()

        val pager = Pager(
            config = PagingConfig(
                pageSize = 40,
                initialLoadSize = 40,
                prefetchDistance = 10,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                FollowedChannelsDataSource(
                    userId = appUser.userId,
                    twitchApi = twitchApi,
                )
            },
        )

        return pager.flow.map { page ->
            page.flatMap { follows ->
                val completeUsers: Map<String, User> =
                    follows
                        .map { follow -> follow.user }
                        .mapWithProfileImages()
                        .associateBy { user -> user.id }

                follows.map { follow ->
                    follow.copy(
                        user = completeUsers[follow.user.id] ?: follow.user,
                    )
                }
            }
        }
    }

    override suspend fun loadStream(userId: String): Result<Stream> =
        withContext(DispatchersProvider.io) {
            twitchApi.getStreams(ids = listOf(userId))
                .mapCatching { response ->
                    val stream = response.data.firstOrNull()
                        ?: error("Stream for userId $userId not found")

                    Stream(
                        id = stream.id,
                        user = User(
                            id = stream.userId,
                            login = stream.userLogin,
                            displayName = stream.userName,

                        ),
                        gameName = stream.gameName,
                        title = stream.title,
                        viewerCount = stream.viewerCount,
                        startedAt = stream.startedAt,
                        tags = stream.tags.toPersistentList(),
                    )
                }
        }

    override suspend fun loadUsersById(ids: List<String>): Result<List<User>> =
        withContext(DispatchersProvider.io) {
            twitchApi.getUsersById(ids = ids)
                .map { response ->
                    response.data.map { user ->
                        User(
                            id = user.id,
                            login = user.login,
                            displayName = user.displayName,
                            description = user.description,
                            profileImageUrl = user.profileImageUrl,
                            createdAt = user.createdAt,
                        )
                    }
                }
        }

    override suspend fun loadUsersByLogin(logins: List<String>): Result<List<User>> =
        withContext(DispatchersProvider.io) {
            twitchApi.getUsersByLogin(logins = logins)
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
                        )
                    }
                }
        }

    override suspend fun loadUserByLogin(login: String): Result<User> {
        return loadUsersByLogin(logins = listOf(login))
            .mapCatching { users ->
                if (users.isEmpty()) {
                    error("No user found for login: $login")
                }

                users.first()
            }
    }

    override suspend fun loadCheerEmotes(userId: String): Result<List<Emote>> {
        return withContext(DispatchersProvider.io) {
            twitchApi.getCheerEmotes(userId = userId)
                .map { response ->
                    response.data.flatMap { emote ->
                        emote.tiers.map { tier ->
                            tier.map(prefix = emote.prefix)
                        }
                    }
                }
        }
    }

    override suspend fun loadEmotesFromSet(setIds: List<String>): Result<List<Emote>> =
        withContext(DispatchersProvider.io) {
            twitchApi.getEmotesFromSet(setIds = setIds)
                .map { response ->
                    response.data
                        .sortedByDescending { it.setId }
                        .map { emote -> emote.map(templateUrl = response.template) }
                }
        }

    override suspend fun getRecentChannels(): Flow<List<ChannelSearchResult>?> {
        return withContext(DispatchersProvider.io) {
            recentChannelsDao.getAll()
                .map { channels ->
                    val ids = channels.map { channel -> channel.id }
                    twitchApi.getUsersById(ids = ids)
                        .getOrNull()
                        ?.data
                        ?.map { user ->
                            ChannelSearchResult(
                                title = user.displayName,
                                user = User(
                                    id = user.id,
                                    login = user.login,
                                    displayName = user.displayName,
                                    profileImageUrl = user.profileImageUrl,
                                ),
                            )
                        }
                        ?.sortedBy { result ->
                            ids.indexOf(result.user.id)
                        }
                }
        }
    }

    override suspend fun insertRecentChannel(channel: User, usedAt: Instant) {
        withContext(DispatchersProvider.io) {
            recentChannelsDao.insert(
                Recent_channels(
                    id = channel.id,
                    used_at = usedAt.toEpochMilliseconds(),
                ),
            )
        }
    }

    override suspend fun loadChannelSchedule(channelId: String): Result<ChannelSchedule> {
        return withContext(DispatchersProvider.io) {
            twitchApi.getChannelSchedule(
                channelId = channelId,
                limit = 10,
                after = null,
            ).mapCatching { response ->
                ChannelSchedule(
                    segments = response.data.segments.map { segment ->
                        ChannelScheduleSegment(
                            id = segment.id,
                            startTime = segment.startTime,
                            endTime = segment.endTime,
                            title = segment.title,
                            canceledUntil = segment.canceledUntil,
                            category = segment.category,
                            isRecurring = segment.isRecurring,
                        )
                    },
                    userId = response.data.userId,
                    userLogin = response.data.userLogin,
                    userDisplayName = response.data.userDisplayName,
                    vacation = response.data.vacation?.let { vacation ->
                        ChannelScheduleVacation(
                            startTime = vacation.startTime,
                            endTime = vacation.endTime,
                        )
                    },
                )
            }
        }
    }

    private suspend fun Collection<User>.mapWithProfileImages(): List<User> {
        val results = this
        return results
            .map { result -> result.id }
            .chunked(size = 100)
            .flatMap { idsToUpdate ->
                val users = twitchApi.getUsersById(ids = idsToUpdate)
                    .fold(
                        onSuccess = { response -> response.data },
                        onFailure = { exception ->
                            logError<TwitchRepositoryImpl>(exception) { "Failed to load user profiles" }
                            emptyList()
                        },
                    )

                results.map { result ->
                    result.copy(
                        profileImageUrl = users
                            .firstOrNull { user -> user.id == result.id }
                            ?.profileImageUrl,
                    )
                }
            }
    }
}
