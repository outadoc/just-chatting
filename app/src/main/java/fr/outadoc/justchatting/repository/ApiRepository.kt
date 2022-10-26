package fr.outadoc.justchatting.repository

import androidx.paging.PagedList
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
    ): Listing<ChannelSearch> {
        val factory = SearchChannelsDataSource.Factory(
            query = query,
            helixClientId = authPrefs.helixClientId.first(),
            helixToken = userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix(),
            helixApi = helix,
            coroutineScope = coroutineScope
        )

        val config = PagedList.Config.Builder()
            .setPageSize(15)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(5)
            .setEnablePlaceholders(false)
            .build()

        return createListing(factory, config)
    }

    override suspend fun loadFollowedStreams(coroutineScope: CoroutineScope): Listing<Stream> {
        val factory = FollowedStreamsDataSource.Factory(
            userId = userPreferencesRepository.appUser.first().id,
            helixClientId = authPrefs.helixClientId.first(),
            helixToken = userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix(),
            helixApi = helix,
            coroutineScope = coroutineScope
        )

        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(30)
            .setInitialLoadSizeHint(30)
            .setPrefetchDistance(10)
            .build()

        return createListing(factory, config)
    }

    override suspend fun loadFollowedChannels(
        sort: Sort,
        order: Order,
        coroutineScope: CoroutineScope
    ): Listing<Follow> {
        val factory = FollowedChannelsDataSource.Factory(
            userId = userPreferencesRepository.appUser.first().id,
            helixClientId = authPrefs.helixClientId.first(),
            helixToken = userPreferencesRepository.appUser.first().helixToken?.withBearerPrefix(),
            helixApi = helix,
            sort = sort,
            order = order,
            coroutineScope = coroutineScope
        )

        val config = PagedList.Config.Builder()
            .setPageSize(40)
            .setInitialLoadSizeHint(40)
            .setPrefetchDistance(10)
            .setEnablePlaceholders(false)
            .build()

        return createListing(factory, config)
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
