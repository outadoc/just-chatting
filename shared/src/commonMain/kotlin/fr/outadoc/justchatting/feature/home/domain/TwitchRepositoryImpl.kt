package fr.outadoc.justchatting.feature.home.domain

import androidx.paging.PagingData
import androidx.paging.flatMap
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.home.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleForDay
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.recent.domain.LocalUsersApi
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

internal class TwitchRepositoryImpl(
    private val twitchApi: TwitchApi,
    private val preferencesRepository: PreferenceRepository,
    private val localUsersApi: LocalUsersApi,
) : TwitchRepository {

    private val userSyncLock = Mutex()

    override suspend fun searchChannels(query: String): Flow<PagingData<ChannelSearchResult>> =
        withContext(DispatchersProvider.io) {
            twitchApi
                .searchChannels(query)
                .map { pagingData ->
                    pagingData.flatMap { results ->
                        results.forEach { result ->
                            localUsersApi.rememberUser(userId = result.user.id)
                        }

                        val fullUsersById: Map<String, User> =
                            getUsersById(ids = results.map { result -> result.user.id })
                                .first()
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
                                follows.forEach { follow ->
                                    localUsersApi.rememberUser(userId = follow.user.id)
                                }

                                val fullUsersById: Map<String, User> =
                                    getUsersById(ids = follows.map { follow -> follow.user.id })
                                        .first()
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

    override suspend fun getFollowedChannels(): Flow<List<ChannelFollow>> =
        withContext(DispatchersProvider.io) {
            val prefs = preferencesRepository.currentPreferences.first()
            when (prefs.appUser) {
                is AppUser.LoggedIn -> {
                    localUsersApi
                        .getFollowedChannels()
                        .onStart {
                            syncLocalFollows(appUserId = prefs.appUser.userId)
                            syncLocalUserInfo()
                        }
                        .flowOn(DispatchersProvider.io)
                }

                else -> {
                    flowOf(emptyList())
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
        localUsersApi
            .getUsersById(ids)
            .map { users -> Result.success(users) }
            .onStart { syncLocalUserInfo() }
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
        localUsersApi
            .getUsersByLogin(logins)
            .map { users -> Result.success(users) }
            .onStart { syncLocalUserInfo() }
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
            localUsersApi
                .getRecentChannels()
                .map { users ->
                    users.map { user ->
                        ChannelSearchResult(
                            title = user.displayName,
                            user = User(
                                id = user.id,
                                login = user.login,
                                displayName = user.displayName,
                                description = user.description,
                                profileImageUrl = user.profileImageUrl,
                                createdAt = user.createdAt,
                                usedAt = user.usedAt,
                            ),
                        )
                    }
                }
        }

    override suspend fun insertRecentChannel(channel: User, usedAt: Instant) {
        withContext(DispatchersProvider.io) {
            localUsersApi.rememberUser(
                userId = channel.id,
                usedAt = usedAt,
            )
        }
    }

    override suspend fun getChannelSchedule(
        channelId: String,
        start: Instant,
        timeZone: TimeZone,
    ): Flow<PagingData<ChannelScheduleForDay>> =
        withContext(DispatchersProvider.io) {
            twitchApi.getChannelSchedule(
                channelId = channelId,
                start = start,
                pastRange = EpgConfig.MaxDaysAhead,
                futureRange = EpgConfig.MaxDaysAhead,
                timeZone = timeZone,
            )
        }

    override suspend fun getGlobalBadges(): Result<List<TwitchBadge>> =
        withContext(DispatchersProvider.io) {
            twitchApi.getGlobalBadges()
        }

    override suspend fun getChannelBadges(channelId: String): Result<List<TwitchBadge>> =
        withContext(DispatchersProvider.io) {
            twitchApi.getChannelBadges(channelId)
        }

    private suspend fun syncLocalFollows(appUserId: String): Result<Unit> =
        userSyncLock.withLock {
            twitchApi
                .getFollowedChannels(userId = appUserId)
                .onFailure { exception ->
                    logError<TwitchRepositoryImpl>(exception) {
                        "Error while fetching followed channels"
                    }
                }
                .map { follows ->
                    localUsersApi.replaceFollowedChannels(follows = follows)
                }
        }

    private suspend fun syncLocalUserInfo() {
        userSyncLock.withLock {
            val ids = localUsersApi
                .getUserIdsToUpdate()
                .first()

            logDebug<TwitchRepositoryImpl> { "syncLocalUserInfo: updating ${ids.size} users" }

            localUsersApi.updateUserInfo(
                users = twitchApi.getUsersById(ids)
            )
        }
    }
}
