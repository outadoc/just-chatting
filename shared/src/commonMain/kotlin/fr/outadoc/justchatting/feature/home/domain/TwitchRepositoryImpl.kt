package fr.outadoc.justchatting.feature.home.domain

import androidx.paging.PagingData
import androidx.paging.flatMap
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.home.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.recent.domain.RecentChannelsApi
import fr.outadoc.justchatting.feature.recent.domain.model.RecentChannel
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

@OptIn(ExperimentalCoroutinesApi::class)
internal class TwitchRepositoryImpl(
    private val twitchApi: TwitchApi,
    private val usersMemoryCache: UsersMemoryCache,
    private val preferencesRepository: PreferenceRepository,
    private val recentChannelsApi: RecentChannelsApi,
) : TwitchRepository {

    override suspend fun searchChannels(query: String): Flow<PagingData<ChannelSearchResult>> =
        withContext(DispatchersProvider.io) {
            twitchApi
                .searchChannels(query)
                .map { pagingData ->
                    pagingData.flatMap { results ->
                        val fullUsersById: Map<String, User> =
                            getUsersById(ids = results.map { result -> result.user.id })
                                .last()
                                .getOrNull()
                                .orEmpty()
                                .associateBy { user -> user.id }

                        results.map { result ->
                            result.copy(user = fullUsersById[result.user.id] ?: result.user)
                        }
                    }
                }
        }

    override suspend fun getFollowedStreams(): Flow<PagingData<Stream>> =
        withContext(DispatchersProvider.io) {
            val prefs = preferencesRepository.currentPreferences.first()
            when (prefs.appUser) {
                is AppUser.LoggedIn -> {
                    twitchApi
                        .getFollowedStreams(userId = prefs.appUser.userId)
                        .map { pagingData ->
                            pagingData.flatMap { follows ->
                                val fullUsersById: Map<String, User> =
                                    getUsersById(ids = follows.map { follow -> follow.user.id })
                                        .last()
                                        .getOrNull()
                                        .orEmpty()
                                        .associateBy { user -> user.id }

                                follows.map { follow ->
                                    follow.copy(user = fullUsersById[follow.user.id] ?: follow.user)
                                }
                            }
                        }
                }

                else -> {
                    flowOf(PagingData.empty())
                }
            }
        }

    override suspend fun getFollowedChannels(): Flow<PagingData<ChannelFollow>> =
        withContext(DispatchersProvider.io) {
            val prefs = preferencesRepository.currentPreferences.first()
            when (prefs.appUser) {
                is AppUser.LoggedIn -> {
                    twitchApi
                        .getFollowedChannels(userId = prefs.appUser.userId)
                        .map { pagingData ->
                            pagingData.flatMap { follows ->
                                val fullUsersById: Map<String, User> =
                                    getUsersById(ids = follows.map { follow -> follow.user.id })
                                        .last()
                                        .getOrNull()
                                        .orEmpty()
                                        .associateBy { user -> user.id }

                                follows.map { follow ->
                                    follow.copy(user = fullUsersById[follow.user.id] ?: follow.user)
                                }
                            }
                        }
                }

                else -> {
                    flowOf(PagingData.empty())
                }
            }
        }

    override suspend fun getStreamByUserId(userId: String): Flow<Result<Stream>> =
        flow {
            emit(
                twitchApi
                    .getStreamsByUserId(ids = listOf(userId))
                    .mapCatching { response ->
                        response.firstOrNull()
                            ?: error("Stream for userId $userId not found")
                    },
            )
        }.flowOn(DispatchersProvider.io)

    override suspend fun getStreamByUserLogin(userLogin: String): Flow<Result<Stream>> =
        flow {
            emit(
                twitchApi
                    .getStreamsByUserLogin(logins = listOf(userLogin))
                    .mapCatching { response ->
                        response.firstOrNull()
                            ?: error("Stream for userLogin $userLogin not found")
                    },
            )
        }.flowOn(DispatchersProvider.io)

    override suspend fun getUsersById(ids: List<String>): Flow<Result<List<User>>> =
        flow {
            if (ids.isEmpty()) {
                emit(Result.success(emptyList()))
                return@flow
            }

            val cachedUsers = usersMemoryCache.getUsersById(ids = ids)
            if (cachedUsers.isNotEmpty()) {
                emit(
                    Result.success(cachedUsers),
                )
            }

            emit(
                twitchApi
                    .getUsersById(ids = ids)
                    .onSuccess { users ->
                        usersMemoryCache.put(users)
                    },
            )
        }
            .flowOn(DispatchersProvider.io)

    override suspend fun getUserById(id: String): Flow<Result<User>> =
        withContext(DispatchersProvider.io) {
            getUsersById(ids = listOf(id))
                .map { result ->
                    result.mapCatching { users ->
                        users.firstOrNull()
                            ?: error("No user found for id: $id")
                    }
                }
        }

    override suspend fun getUsersByLogin(logins: List<String>): Flow<Result<List<User>>> =
        flow {
            if (logins.isEmpty()) {
                emit(Result.success(emptyList()))
                return@flow
            }

            val cachedUsers = usersMemoryCache.getUsersByLogin(logins = logins)
            if (cachedUsers.isNotEmpty()) {
                emit(
                    Result.success(cachedUsers),
                )
            }

            emit(
                twitchApi
                    .getUsersByLogin(logins = logins)
                    .onSuccess { users ->
                        usersMemoryCache.put(users)
                    },
            )
        }
            .flowOn(DispatchersProvider.io)

    override suspend fun getUserByLogin(login: String): Flow<Result<User>> =
        withContext(DispatchersProvider.io) {
            getUsersByLogin(logins = listOf(login))
                .map { result ->
                    result.mapCatching { users ->
                        users.firstOrNull()
                            ?: error("No user found for login: $login")
                    }
                }
        }

    override suspend fun getCheerEmotes(userId: String): Result<List<Emote>> =
        withContext(DispatchersProvider.io) {
            twitchApi.getCheerEmotes(userId = userId)
        }

    override suspend fun getEmotesFromSet(setIds: List<String>): Result<List<Emote>> =
        withContext(DispatchersProvider.io) {
            twitchApi.getEmotesFromSet(setIds = setIds)
        }

    override suspend fun getRecentChannels(): Flow<List<ChannelSearchResult>?> =
        withContext(DispatchersProvider.io) {
            recentChannelsApi
                .getAll()
                .flatMapLatest { channels ->
                    val ids = channels.map { channel -> channel.id }
                    flow {
                        val cachedUsers: List<User> =
                            usersMemoryCache.getUsersById(ids = ids)

                        emit(ids to cachedUsers)

                        val remoteUsers: List<User> =
                            twitchApi
                                .getUsersById(ids = ids)
                                .getOrElse { emptyList() }

                        emit(ids to remoteUsers)
                    }
                }
                .onEach { (_, users) ->
                    usersMemoryCache.put(users)
                }
                .map { (ids, users) ->
                    users
                        .map { user ->
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
                        .sortedBy { result ->
                            ids.indexOf(result.user.id)
                        }
                }
        }

    override suspend fun insertRecentChannel(channel: User, usedAt: Instant) {
        withContext(DispatchersProvider.io) {
            recentChannelsApi.insert(
                RecentChannel(
                    id = channel.id,
                    usedAt = usedAt,
                ),
            )
        }
    }

    override suspend fun getChannelSchedule(channelId: String): Flow<PagingData<ChannelScheduleSegment>> =
        withContext(DispatchersProvider.io) {
            twitchApi
                .getChannelSchedule(channelId = channelId)
                .map { pagingData ->
                    pagingData.flatMap { segments -> segments }
                }
        }

    override suspend fun getGlobalBadges(): Result<List<TwitchBadge>> =
        withContext(DispatchersProvider.io) {
            twitchApi.getGlobalBadges()
        }

    override suspend fun getChannelBadges(channelId: String): Result<List<TwitchBadge>> =
        withContext(DispatchersProvider.io) {
            twitchApi.getChannelBadges(channelId)
        }
}
