package fr.outadoc.justchatting.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import fr.outadoc.justchatting.api.HelixApi
import fr.outadoc.justchatting.model.chat.CheerEmote
import fr.outadoc.justchatting.model.chat.TwitchEmote
import fr.outadoc.justchatting.model.helix.channel.ChannelSearch
import fr.outadoc.justchatting.model.helix.channel.ChannelSearchResponse
import fr.outadoc.justchatting.model.helix.follows.Follow
import fr.outadoc.justchatting.model.helix.follows.FollowResponse
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.model.helix.stream.StreamsResponse
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.repository.datasource.FollowedChannelsDataSource
import fr.outadoc.justchatting.repository.datasource.FollowedStreamsDataSource
import fr.outadoc.justchatting.repository.datasource.SearchChannelsDataSource
import fr.outadoc.justchatting.util.withBearerPrefix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ApiRepository(
    private val helix: HelixApi,
    private val authPrefs: AuthPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : TwitchService {

    override suspend fun loadSearchChannels(query: String): Pager<String, ChannelSearchResponse> {
        val helixClientId = authPrefs.helixClientId.first()
        val helixToken = userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix()

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
                    helixClientId = helixClientId,
                    helixToken = helixToken,
                    helixApi = helix
                )
            }
        )
    }

    override suspend fun mapSearchWithUserProfileImages(searchResults: List<ChannelSearch>): List<ChannelSearch> =
        with(searchResults) {
            val helixClientId = authPrefs.helixClientId.first()
            val helixToken =
                userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix()

            return mapNotNull { result -> result.id }
                .chunked(size = 100)
                .flatMap { idsToUpdate ->
                    val users = helix.getUsersById(
                        clientId = helixClientId,
                        token = helixToken,
                        ids = idsToUpdate
                    )
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
        val userId = userPreferencesRepository.appUser.first().id
        val helixClientId = authPrefs.helixClientId.first()
        val helixToken = userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix()

        return Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                FollowedStreamsDataSource(
                    userId = userId,
                    helixClientId = helixClientId,
                    helixToken = helixToken,
                    helixApi = helix
                )
            }
        )
    }

    override suspend fun mapStreamsWithUserProfileImages(streams: Collection<Stream>): List<Stream> =
        with(streams) {
            val helixClientId = authPrefs.helixClientId.first()
            val helixToken =
                userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix()

            val users = mapNotNull { it.userId }
                .chunked(100)
                .flatMap { ids ->
                    helix.getUsersById(
                        clientId = helixClientId,
                        token = helixToken,
                        ids = ids
                    )
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
        val userId = userPreferencesRepository.appUser.first().id
        val helixClientId = authPrefs.helixClientId.first()
        val helixToken = userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix()

        return Pager(
            config = PagingConfig(
                pageSize = 40,
                initialLoadSize = 40,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                FollowedChannelsDataSource(
                    userId = userId,
                    helixClientId = helixClientId,
                    helixToken = helixToken,
                    helixApi = helix
                )
            }
        )
    }

    override suspend fun mapFollowsWithUserProfileImages(follows: Collection<Follow>): Collection<Follow> =
        with(follows) {
            val helixClientId = authPrefs.helixClientId.first()
            val helixToken =
                userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix()

            val results: List<User> =
                filter { follow -> follow.profileImageURL == null }
                    .mapNotNull { follow -> follow.toId }
                    .chunked(size = 100)
                    .flatMap { idsToUpdate ->
                        helix.getUsersById(
                            clientId = helixClientId,
                            token = helixToken,
                            ids = idsToUpdate
                        )
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
            helix.getStreams(
                clientId = authPrefs.helixClientId.first(),
                token = userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix(),
                ids = listOf(channelId)
            ).data?.firstOrNull()
        }

    override suspend fun loadUsersById(ids: List<String>): List<User>? =
        withContext(Dispatchers.IO) {
            helix.getUsersById(
                clientId = authPrefs.helixClientId.first(),
                token = userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix(),
                ids = ids
            ).data
        }

    override suspend fun loadUsersByLogin(logins: List<String>): List<User>? =
        withContext(Dispatchers.IO) {
            helix.getUsersByLogin(
                clientId = authPrefs.helixClientId.first(),
                token = userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix(),
                logins = logins
            ).data
        }

    override suspend fun loadCheerEmotes(userId: String): List<CheerEmote> =
        withContext(Dispatchers.IO) {
            helix.getCheerEmotes(
                clientId = authPrefs.helixClientId.first(),
                token = userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix(),
                userId = userId
            ).emotes
        }

    override suspend fun loadEmotesFromSet(setIds: List<String>): List<TwitchEmote>? =
        withContext(Dispatchers.IO) {
            helix.getEmotesFromSet(
                clientId = authPrefs.helixClientId.first(),
                token = userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix(),
                setIds = setIds
            ).emotes
        }

    override suspend fun loadUserFollowing(
        userId: String?,
        channelId: String?,
        userLogin: String?
    ): Boolean = withContext(Dispatchers.IO) {
        helix.getUserFollows(
            clientId = authPrefs.helixClientId.first(),
            token = userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix(),
            userId = userId,
            channelId = channelId
        ).total == 1
    }
}
