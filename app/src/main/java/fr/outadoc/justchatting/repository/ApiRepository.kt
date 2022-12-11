package fr.outadoc.justchatting.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import fr.outadoc.justchatting.api.HelixApi
import fr.outadoc.justchatting.component.twitch.model.helix.channel.ChannelSearch
import fr.outadoc.justchatting.component.twitch.model.helix.channel.ChannelSearchResponse
import fr.outadoc.justchatting.component.twitch.model.helix.follows.Follow
import fr.outadoc.justchatting.component.twitch.model.helix.follows.FollowResponse
import fr.outadoc.justchatting.component.twitch.model.helix.stream.Stream
import fr.outadoc.justchatting.component.twitch.model.helix.stream.StreamsResponse
import fr.outadoc.justchatting.component.twitch.model.helix.user.User
import fr.outadoc.justchatting.component.twitch.model.chat.CheerEmote
import fr.outadoc.justchatting.component.twitch.model.chat.TwitchEmote
import fr.outadoc.justchatting.repository.datasource.FollowedChannelsDataSource
import fr.outadoc.justchatting.repository.datasource.FollowedStreamsDataSource
import fr.outadoc.justchatting.repository.datasource.SearchChannelsDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ApiRepository(
    private val helix: HelixApi,
    private val preferencesRepository: PreferenceRepository
) : TwitchService {

    override suspend fun loadSearchChannels(query: String): Pager<String, ChannelSearchResponse> {
        return Pager(
            config = PagingConfig(
                pageSize = 15,
                initialLoadSize = 15,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SearchChannelsDataSource(
                    query = query,
                    helixApi = helix
                )
            }
        )
    }

    override suspend fun mapSearchWithUserProfileImages(searchResults: List<ChannelSearch>): List<ChannelSearch> =
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
                                ?.profileImageUrl
                        )
                    }
                }
        }

    override suspend fun loadFollowedStreams(): Pager<String, StreamsResponse> {
        val prefs = preferencesRepository.currentPreferences.first()
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                FollowedStreamsDataSource(
                    userId = prefs.appUser.id,
                    helixApi = helix
                )
            }
        )
    }

    override suspend fun mapStreamsWithUserProfileImages(streams: Collection<Stream>): List<Stream> =
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
                    profileImageURL = user?.profileImageUrl
                )
            }
        }

    override suspend fun loadFollowedChannels(): Pager<String, FollowResponse> {
        val prefs = preferencesRepository.currentPreferences.first()
        return Pager(
            config = PagingConfig(
                pageSize = 40,
                initialLoadSize = 40,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                FollowedChannelsDataSource(
                    userId = prefs.appUser.id,
                    helixApi = helix
                )
            }
        )
    }

    override suspend fun mapFollowsWithUserProfileImages(follows: Collection<Follow>): Collection<Follow> =
        with(follows) {
            val results: List<User> =
                filter { follow -> follow.profileImageURL == null }
                    .mapNotNull { follow -> follow.toId }
                    .chunked(size = 100)
                    .flatMap { idsToUpdate ->
                        helix.getUsersById(ids = idsToUpdate)
                            .data
                            .orEmpty()
                    }

            return map { follow ->
                val userInfo = results.firstOrNull { user -> user.id == follow.toId }
                follow.copy(
                    profileImageURL = userInfo?.profileImageUrl
                )
            }
        }

    override suspend fun loadStreamWithUser(channelId: String): Stream? =
        withContext(Dispatchers.IO) {
            helix.getStreams(ids = listOf(channelId))
                .data
                ?.firstOrNull()
        }

    override suspend fun loadUsersById(ids: List<String>): List<User>? =
        withContext(Dispatchers.IO) {
            helix.getUsersById(ids = ids).data
        }

    override suspend fun loadUsersByLogin(logins: List<String>): List<User>? =
        withContext(Dispatchers.IO) {
            helix.getUsersByLogin(logins = logins).data
        }

    override suspend fun loadCheerEmotes(userId: String): List<CheerEmote> =
        withContext(Dispatchers.IO) {
            helix.getCheerEmotes(userId = userId).emotes
        }

    override suspend fun loadEmotesFromSet(setIds: List<String>): List<TwitchEmote>? =
        withContext(Dispatchers.IO) {
            helix.getEmotesFromSet(setIds = setIds).emotes
        }

    override suspend fun loadUserFollowing(
        userId: String?,
        channelId: String?,
        userLogin: String?
    ): Boolean = withContext(Dispatchers.IO) {
        helix.getUserFollows(
            userId = userId,
            channelId = channelId
        ).total == 1
    }
}
