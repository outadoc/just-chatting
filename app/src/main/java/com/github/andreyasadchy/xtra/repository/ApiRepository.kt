package com.github.andreyasadchy.xtra.repository

import androidx.paging.PagedList
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.chat.CheerEmote
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.model.helix.channel.ChannelSearch
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.model.helix.follows.Order
import com.github.andreyasadchy.xtra.model.helix.follows.Sort
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.model.helix.user.User
import com.github.andreyasadchy.xtra.repository.datasource.FollowedChannelsDataSource
import com.github.andreyasadchy.xtra.repository.datasource.FollowedStreamsDataSource
import com.github.andreyasadchy.xtra.repository.datasource.SearchChannelsDataSource
import com.github.andreyasadchy.xtra.util.addTokenPrefixHelix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiRepository @Inject constructor(
    private val helix: HelixApi,
    private val localFollowsChannel: LocalFollowChannelRepository,
    private val authPrefs: AuthPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : TwitchService {

    override suspend fun loadSearchChannels(
        query: String,
        coroutineScope: CoroutineScope,
    ): Listing<ChannelSearch> {
        val factory = SearchChannelsDataSource.Factory(
            query = query,
            helixClientId = authPrefs.helixClientId.first(),
            helixToken = userPreferencesRepository.user.first().helixToken?.addTokenPrefixHelix(),
            helixApi = helix,
            coroutineScope = coroutineScope
        )

        val config = PagedList.Config.Builder()
            .setPageSize(15)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(5)
            .setEnablePlaceholders(false)
            .build()

        return Listing.create(factory, config)
    }

    override suspend fun loadFollowedStreams(coroutineScope: CoroutineScope): Listing<Stream> {
        val factory = FollowedStreamsDataSource.Factory(
            localFollowsChannel = localFollowsChannel,
            userId = userPreferencesRepository.user.first().id,
            helixClientId = authPrefs.helixClientId.first(),
            helixToken = userPreferencesRepository.user.first().helixToken?.addTokenPrefixHelix(),
            helixApi = helix,
            coroutineScope = coroutineScope
        )

        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(30)
            .setInitialLoadSizeHint(30)
            .setPrefetchDistance(10)
            .build()

        return Listing.create(factory, config)
    }

    override suspend fun loadFollowedChannels(
        sort: Sort,
        order: Order,
        coroutineScope: CoroutineScope,
    ): Listing<Follow> {
        val factory = FollowedChannelsDataSource.Factory(
            localFollowsChannel = localFollowsChannel,
            userId = userPreferencesRepository.user.first().id,
            helixClientId = authPrefs.helixClientId.first(),
            helixToken = userPreferencesRepository.user.first().helixToken?.addTokenPrefixHelix(),
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

        return Listing.create(factory, config)
    }

    override suspend fun loadStreamWithUser(channelId: String): Stream? =
        withContext(Dispatchers.IO) {
            helix.getStreams(
                clientId = authPrefs.helixClientId.first(),
                token = userPreferencesRepository.user.first().helixToken?.addTokenPrefixHelix(),
                ids = listOf(channelId)
            ).data?.firstOrNull()
        }

    override suspend fun loadUsersById(ids: List<String>): List<User>? =
        withContext(Dispatchers.IO) {
            helix.getUsersById(
                clientId = authPrefs.helixClientId.first(),
                token = userPreferencesRepository.user.first().helixToken?.addTokenPrefixHelix(),
                ids = ids
            ).data
        }

    override suspend fun loadUsersByLogin(logins: List<String>): List<User>? =
        withContext(Dispatchers.IO) {
            helix.getUsersByLogin(
                clientId = authPrefs.helixClientId.first(),
                token = userPreferencesRepository.user.first().helixToken?.addTokenPrefixHelix(),
                logins = logins
            ).data
        }

    override suspend fun loadCheerEmotes(userId: String): List<CheerEmote> =
        withContext(Dispatchers.IO) {
            helix.getCheerEmotes(
                clientId = authPrefs.helixClientId.first(),
                token = userPreferencesRepository.user.first().helixToken?.addTokenPrefixHelix(),
                userId = userId
            ).emotes
        }

    override suspend fun loadEmotesFromSet(setIds: List<String>): List<TwitchEmote>? =
        withContext(Dispatchers.IO) {
            helix.getEmotesFromSet(
                clientId = authPrefs.helixClientId.first(),
                token = userPreferencesRepository.user.first().helixToken?.addTokenPrefixHelix(),
                setIds = setIds
            ).emotes
        }

    override suspend fun loadUserFollowing(
        userId: String?,
        channelId: String?,
        userLogin: String?,
    ): Boolean = withContext(Dispatchers.IO) {
        helix.getUserFollows(
            clientId = authPrefs.helixClientId.first(),
            token = userPreferencesRepository.user.first().helixToken?.addTokenPrefixHelix(),
            userId = userId,
            channelId = channelId
        ).total == 1
    }
}
