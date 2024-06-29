package fr.outadoc.justchatting.feature.home.domain

import androidx.paging.PagingData
import fr.outadoc.justchatting.data.db.Recent_channels
import fr.outadoc.justchatting.feature.emotes.data.recent.RecentChannelsDao
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.home.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSchedule
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.core.DispatchersProvider
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

    override suspend fun searchChannels(query: String): Flow<PagingData<ChannelSearchResult>> {
        return withContext(DispatchersProvider.io) {
            twitchApi.searchChannels(query)
        }
    }

    override suspend fun getFollowedStreams(): Flow<PagingData<Stream>> {
        return withContext(DispatchersProvider.io) {
            when (val appUser: AppUser = preferencesRepository.currentPreferences.first().appUser) {
                is AppUser.LoggedIn -> {
                    twitchApi.getFollowedStreams(userId = appUser.userId)
                }

                else -> {
                    emptyFlow()
                }
            }
        }
    }

    override suspend fun getFollowedChannels(): Flow<PagingData<ChannelFollow>> {
        return withContext(DispatchersProvider.io) {
            when (val appUser: AppUser = preferencesRepository.currentPreferences.first().appUser) {
                is AppUser.LoggedIn -> {
                    twitchApi.getFollowedChannels(userId = appUser.userId)
                }

                else -> {
                    emptyFlow()
                }
            }
        }
    }

    override suspend fun getStream(userId: String): Result<Stream> =
        withContext(DispatchersProvider.io) {
            twitchApi.getStreams(ids = listOf(userId))
                .mapCatching { response ->
                    response.firstOrNull()
                        ?: error("Stream for userId $userId not found")
                }
        }

    override suspend fun getUsersById(ids: List<String>): Result<List<User>> =
        withContext(DispatchersProvider.io) {
            twitchApi.getUsersById(ids = ids)
        }

    override suspend fun getUsersByLogin(logins: List<String>): Result<List<User>> =
        withContext(DispatchersProvider.io) {
            twitchApi.getUsersByLogin(logins = logins)
        }

    override suspend fun getUserByLogin(login: String): Result<User> {
        return getUsersByLogin(logins = listOf(login))
            .mapCatching { users ->
                if (users.isEmpty()) {
                    error("No user found for login: $login")
                }

                users.first()
            }
    }

    override suspend fun getCheerEmotes(userId: String): Result<List<Emote>> {
        return withContext(DispatchersProvider.io) {
            twitchApi.getCheerEmotes(userId = userId)
        }
    }

    override suspend fun getEmotesFromSet(setIds: List<String>): Result<List<Emote>> =
        withContext(DispatchersProvider.io) {
            twitchApi.getEmotesFromSet(setIds = setIds)
        }

    override suspend fun getRecentChannels(): Flow<List<ChannelSearchResult>?> {
        return withContext(DispatchersProvider.io) {
            recentChannelsDao.getAll()
                .map { channels ->
                    val ids = channels.map { channel -> channel.id }
                    twitchApi.getUsersById(ids = ids)
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
            )
        }
    }
}
