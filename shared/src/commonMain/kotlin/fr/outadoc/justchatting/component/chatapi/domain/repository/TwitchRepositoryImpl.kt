package fr.outadoc.justchatting.component.chatapi.domain.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.flatMap
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelFollow
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSearch
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import fr.outadoc.justchatting.component.chatapi.domain.repository.datasource.FollowedChannelsDataSource
import fr.outadoc.justchatting.component.chatapi.domain.repository.datasource.FollowedStreamsDataSource
import fr.outadoc.justchatting.component.chatapi.domain.repository.datasource.SearchChannelsDataSource
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.component.twitch.http.api.HelixApi
import fr.outadoc.justchatting.component.twitch.utils.map
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TwitchRepositoryImpl(
    private val helix: HelixApi,
    private val preferencesRepository: PreferenceRepository,
) : TwitchRepository {

    override suspend fun loadSearchChannels(query: String): Flow<PagingData<ChannelSearch>> {
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
                    helixApi = helix,
                )
            },
        )

        return pager.flow.map { page ->
            page.flatMap { searchResponse ->
                mapSearchWithUserProfileImages(searchResponse)
            }
        }
    }

    private suspend fun mapSearchWithUserProfileImages(searchResults: List<ChannelSearch>): List<ChannelSearch> =
        with(searchResults) {
            return mapNotNull { result -> result.id }
                .chunked(size = 100)
                .flatMap { idsToUpdate ->
                    val users = helix.getUsersById(ids = idsToUpdate)
                        .data
                        .orEmpty()

                    map { searchResult ->
                        searchResult.copy(
                            profileImageURL = users.firstOrNull { user -> user.id == searchResult.id }
                                ?.profileImageUrl,
                        )
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
                    helixApi = helix,
                )
            },
        )

        return pager.flow.map { page ->
            page.flatMap { streams ->
                streams
                    .associateBy { stream -> stream.userId }
                    .values
                    .let { stream -> mapStreamsWithUserProfileImages(stream) }
            }
        }
    }

    private suspend fun mapStreamsWithUserProfileImages(streams: Collection<Stream>): List<Stream> =
        with(streams) {
            val users = mapNotNull { it.userId }
                .chunked(100)
                .flatMap { ids ->
                    helix.getUsersById(ids = ids)
                        .data
                        .orEmpty()
                }

            return map { stream ->
                val user = users.firstOrNull { user -> stream.userId == user.id }
                stream.copy(
                    profileImageURL = user?.profileImageUrl,
                )
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
                    helixApi = helix,
                )
            },
        )

        return pager.flow.map { page ->
            page.flatMap { follows ->
                mapFollowsWithUserProfileImages(follows)
            }
        }
    }

    private suspend fun mapFollowsWithUserProfileImages(follows: Collection<ChannelFollow>): Collection<ChannelFollow> =
        with(follows) {
            val results: List<User> =
                filter { follow -> follow.profileImageURL == null }
                    .mapNotNull { follow -> follow.userId }
                    .chunked(size = 100)
                    .flatMap { idsToUpdate ->
                        helix.getUsersById(ids = idsToUpdate)
                            .data
                            .orEmpty()
                    }
                    .map { user ->
                        User(
                            id = user.id,
                            login = user.login,
                            displayName = user.displayName,
                            description = user.description,
                            profileImageUrl = user.profileImageUrl,
                            createdAt = user.createdAt,
                        )
                    }

            return map { follow ->
                val userInfo = results.firstOrNull { user -> user.id == follow.userId }
                follow.copy(
                    profileImageURL = userInfo?.profileImageUrl,
                )
            }
        }

    override suspend fun loadStream(userId: String): Stream? =
        withContext(DispatchersProvider.io) {
            helix.getStreams(ids = listOf(userId))
                .data
                .firstOrNull()
                ?.let { stream ->
                    Stream(
                        id = stream.id,
                        userId = stream.userId,
                        userLogin = stream.userLogin,
                        userName = stream.userName,
                        gameName = stream.gameName,
                        title = stream.title,
                        viewerCount = stream.viewerCount,
                        startedAt = stream.startedAt,
                        tags = stream.tags,
                    )
                }
        }

    override suspend fun loadUsersById(ids: List<String>): List<User>? =
        withContext(DispatchersProvider.io) {
            helix.getUsersById(ids = ids).data?.map { user ->
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

    override suspend fun loadUsersByLogin(logins: List<String>): List<User>? =
        withContext(DispatchersProvider.io) {
            helix.getUsersByLogin(logins = logins).data?.map { user ->
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

    override suspend fun loadCheerEmotes(userId: String): List<Emote> =
        withContext(DispatchersProvider.io) {
            helix.getCheerEmotes(userId = userId)
                .data
                .flatMap { emote ->
                    emote.tiers.map { tier ->
                        tier.map(prefix = emote.prefix)
                    }
                }
        }

    override suspend fun loadEmotesFromSet(setIds: List<String>): List<Emote> =
        withContext(DispatchersProvider.io) {
            val response = helix.getEmotesFromSet(setIds = setIds)
            response.data
                .sortedByDescending { it.setId }
                .map { emote -> emote.map(templateUrl = response.template) }
        }
}
