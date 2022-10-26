package fr.outadoc.justchatting.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import fr.outadoc.justchatting.api.HelixApi
import fr.outadoc.justchatting.model.chat.CheerEmote
import fr.outadoc.justchatting.model.chat.TwitchEmote
import fr.outadoc.justchatting.model.helix.channel.ChannelSearch
import fr.outadoc.justchatting.model.helix.follows.Follow
import fr.outadoc.justchatting.model.helix.follows.Order
import fr.outadoc.justchatting.model.helix.follows.Sort
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.repository.datasource.FollowedChannelsDataSource
import fr.outadoc.justchatting.repository.datasource.FollowedStreamsDataSource
import fr.outadoc.justchatting.repository.datasource.SearchChannelsDataSource
import fr.outadoc.justchatting.util.withBearerPrefix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ApiRepository(
    private val helix: HelixApi,
    private val authPrefs: AuthPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : TwitchService {

    override suspend fun loadSearchChannels(
        query: String,
        coroutineScope: CoroutineScope
    ): Pager<Int, ChannelSearch> {
        val factory = SearchChannelsDataSource.Factory(
            query = query,
            helixClientId = authPrefs.helixClientId.first(),
            helixToken = userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix(),
            helixApi = helix,
            coroutineScope = coroutineScope
        )

        return Pager(
            config = PagingConfig(
                pageSize = 15,
                initialLoadSize = 15,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            pagingSourceFactory = factory.asPagingSourceFactory()
        )
    }

    override suspend fun loadFollowedStreams(coroutineScope: CoroutineScope): Pager<Int, Stream> {
        val factory = FollowedStreamsDataSource.Factory(
            userId = userPreferencesRepository.appUser.first().id,
            helixClientId = authPrefs.helixClientId.first(),
            helixToken = userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix(),
            helixApi = helix,
            coroutineScope = coroutineScope
        )

        return Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = factory.asPagingSourceFactory()
        )
    }

    override suspend fun loadFollowedChannels(
        sort: Sort,
        order: Order,
        coroutineScope: CoroutineScope
    ): Pager<Int, Follow> {
        val factory = FollowedChannelsDataSource.Factory(
            userId = userPreferencesRepository.appUser.first().id,
            helixClientId = authPrefs.helixClientId.first(),
            helixToken = userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix(),
            helixApi = helix,
            sort = sort,
            order = order,
            coroutineScope = coroutineScope
        )

        return Pager(
            config = PagingConfig(
                pageSize = 40,
                initialLoadSize = 40,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = factory.asPagingSourceFactory()
        )
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
