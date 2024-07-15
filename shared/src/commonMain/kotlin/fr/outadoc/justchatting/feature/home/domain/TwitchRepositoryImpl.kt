package fr.outadoc.justchatting.feature.home.domain

import androidx.paging.PagingData
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.home.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSchedule
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.recent.domain.RecentChannelsApi
import fr.outadoc.justchatting.feature.recent.domain.model.RecentChannel
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

internal class TwitchRepositoryImpl(
    private val twitchApi: TwitchApi,
    private val preferencesRepository: PreferenceRepository,
    private val recentChannelsApi: RecentChannelsApi,
) : TwitchRepository {

    override suspend fun searchChannels(query: String): Flow<PagingData<ChannelSearchResult>> =
        withContext(DispatchersProvider.io) {
            twitchApi.searchChannels(query)
        }

    override suspend fun getFollowedStreams(): Flow<PagingData<Stream>> =
        withContext(DispatchersProvider.io) {
            when (val appUser: AppUser = preferencesRepository.currentPreferences.first().appUser) {
                is AppUser.LoggedIn -> {
                    twitchApi.getFollowedStreams(userId = appUser.userId)
                }

                else -> {
                    emptyFlow()
                }
            }
        }

    override suspend fun getFollowedChannels(): Flow<PagingData<ChannelFollow>> =
        withContext(DispatchersProvider.io) {
            when (val appUser: AppUser = preferencesRepository.currentPreferences.first().appUser) {
                is AppUser.LoggedIn -> {
                    twitchApi.getFollowedChannels(userId = appUser.userId)
                }

                else -> {
                    emptyFlow()
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
        flow { emit(twitchApi.getUsersById(ids = ids)) }
            .flowOn(DispatchersProvider.io)

    override suspend fun getUsersByLogin(logins: List<String>): Flow<Result<List<User>>> =
        flow { emit(twitchApi.getUsersByLogin(logins = logins)) }
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
            recentChannelsApi.getAll()
                .map { channels ->
                    val ids = channels.map { channel -> channel.id }
                    twitchApi
                        .getUsersById(ids = ids)
                        .getOrNull()
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

    override suspend fun getChannelSchedule(channelId: String): Result<ChannelSchedule> =
        withContext(DispatchersProvider.io) {
            twitchApi.getChannelSchedule(
                channelId = channelId,
                limit = 10,
                after = null,
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
}
