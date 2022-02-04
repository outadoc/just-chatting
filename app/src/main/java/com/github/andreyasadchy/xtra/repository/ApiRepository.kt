package com.github.andreyasadchy.xtra.repository

import android.util.Log
import androidx.paging.PagedList
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.CheerEmotesQuery
import com.github.andreyasadchy.xtra.StreamsQuery
import com.github.andreyasadchy.xtra.UserQuery
import com.github.andreyasadchy.xtra.VideoQuery
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.api.MiscApi
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.chat.CheerEmote
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.model.chat.VideoMessagesResponse
import com.github.andreyasadchy.xtra.model.helix.channel.ChannelSearch
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.model.helix.user.User
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.repository.datasource.*
import com.github.andreyasadchy.xtra.repository.datasourceGQL.SearchChannelsDataSourceGQL
import com.github.andreyasadchy.xtra.repository.datasourceGQL.SearchGamesDataSourceGQL
import com.github.andreyasadchy.xtra.repository.datasourceGQLquery.*
import com.github.andreyasadchy.xtra.type.ClipsPeriod
import com.github.andreyasadchy.xtra.type.VideoSort
import com.github.andreyasadchy.xtra.ui.view.chat.animateGifs
import com.github.andreyasadchy.xtra.ui.view.chat.emoteQuality
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ApiRepository"

@Singleton
class ApiRepository @Inject constructor(
    private val api: HelixApi,
    private val gql: GraphQLRepository,
    private val misc: MiscApi,
    private val localFollows: LocalFollowRepository) : TwitchService {

    override fun loadTopGames(clientId: String?, userToken: String?, coroutineScope: CoroutineScope): Listing<Game> {
        val factory = GamesDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(30)
                .setInitialLoadSizeHint(30)
                .setPrefetchDistance(10)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadStream(clientId: String?, userToken: String?, channelId: String): Stream? = withContext(Dispatchers.IO) {
        val userIds = mutableListOf<String>()
        userIds.add(channelId)
        api.getStreams(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, userIds).data?.firstOrNull()
    }

    override fun loadTopStreams(clientId: String?, userToken: String?, gameId: String?, languages: String?, thumbnailsEnabled: Boolean, coroutineScope: CoroutineScope): Listing<Stream> {
        val factory = StreamsDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, gameId, languages, api, coroutineScope)
        val builder = PagedList.Config.Builder().setEnablePlaceholders(false)
        if (thumbnailsEnabled) {
            builder.setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
        } else {
            builder.setPageSize(30)
                .setInitialLoadSizeHint(30)
                .setPrefetchDistance(10)
        }
        val config = builder.build()
        return Listing.create(factory, config)
    }

    override fun loadFollowedStreams(useHelix: Boolean, gqlClientId: String?, helixClientId: String?, userToken: String?, userId: String, thumbnailsEnabled: Boolean, coroutineScope: CoroutineScope): Listing<Stream> {
        val factory = FollowedStreamsDataSource.Factory(useHelix, localFollows, this, gqlClientId, helixClientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, userId, api, coroutineScope)
        val builder = PagedList.Config.Builder().setEnablePlaceholders(false)
        if (thumbnailsEnabled) {
            builder.setPageSize(10)
                    .setInitialLoadSizeHint(15)
                    .setPrefetchDistance(3)
        } else {
            builder.setPageSize(30)
                    .setInitialLoadSizeHint(30)
                    .setPrefetchDistance(10)
        }
        val config = builder.build()
        return Listing.create(factory, config)
    }

    override fun loadClips(clientId: String?, userToken: String?, channelId: String?, channelLogin: String?, gameId: String?, started_at: String?, ended_at: String?, coroutineScope: CoroutineScope): Listing<Clip> {
        val factory = ClipsDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, channelId, channelLogin, gameId, started_at, ended_at, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadVideo(clientId: String?, userToken: String?, videoId: String): Video? = withContext(Dispatchers.IO) {
        api.getVideo(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, videoId).data?.firstOrNull()
    }

    override fun loadVideos(clientId: String?, userToken: String?, gameId: String?, period: Period, broadcastType: BroadcastType, language: String?, sort: Sort, coroutineScope: CoroutineScope): Listing<Video> {
        val factory = VideosDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, gameId, period, broadcastType, language, sort, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override fun loadChannelVideos(clientId: String?, userToken: String?, channelId: String, period: Period, broadcastType: BroadcastType, sort: Sort, coroutineScope: CoroutineScope): Listing<Video> {
        val factory = ChannelVideosDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, channelId, period, broadcastType, sort, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadUserById(clientId: String?, userToken: String?, id: String): User? = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading user by id $id")
        val userIds = mutableListOf<String>()
        userIds.add(id)
        api.getUserById(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, userIds).data?.firstOrNull()
    }

    override fun loadSearchGames(clientId: String?, userToken: String?, query: String, coroutineScope: CoroutineScope): Listing<Game> {
        Log.d(TAG, "Loading games containing: $query")
        val factory = GamesSearchDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, query, api, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(15)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(5)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadSearchChannels(clientId: String?, userToken: String?, query: String, coroutineScope: CoroutineScope): Listing<ChannelSearch> {
        Log.d(TAG, "Loading channels containing: $query")
        val factory = ChannelsSearchDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, query, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(15)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(5)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadUserFollows(clientId: String?, userToken: String?, userId: String, channelId: String): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading if user is following channel $channelId")
        api.getUserFollows(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, userId, channelId).total == 1
    }

    override fun loadFollowedChannels(clientId: String?, userToken: String?, userId: String, coroutineScope: CoroutineScope): Listing<Follow> {
        val factory = FollowedChannelsDataSource.Factory(localFollows, clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, userId, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(40)
                .setInitialLoadSizeHint(40)
                .setPrefetchDistance(10)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadEmotesFromSet(clientId: String?, userToken: String?, setIds: List<String>): List<TwitchEmote>? = withContext(Dispatchers.IO) {
        api.getEmotesFromSet(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, setIds).emotes
    }

    override suspend fun loadCheerEmotes(clientId: String?, userToken: String?, userId: String): List<CheerEmote> = withContext(Dispatchers.IO) {
        api.getCheerEmotes(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, userId).emotes
    }

    override suspend fun loadVideoChatLog(clientId: String?, videoId: String, offsetSeconds: Double): VideoMessagesResponse = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading chat log for video $videoId. Offset in seconds: $offsetSeconds")
        misc.getVideoChatLog(clientId, videoId, offsetSeconds, 100)
    }

    override suspend fun loadVideoChatAfter(clientId: String?, videoId: String, cursor: String): VideoMessagesResponse = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading chat log for video $videoId. Cursor: $cursor")
        misc.getVideoChatLogAfter(clientId, videoId, cursor, 100)
    }


    override suspend fun loadStreamGQL(clientId: String?, channelId: String): Stream? = withContext(Dispatchers.IO) {
        val userIds = mutableListOf<String>()
        userIds.add(channelId)
        val get = apolloClient(XtraModule(), clientId).query(StreamsQuery(Optional.Present(userIds))).execute().data?.users?.firstOrNull()
        if (get != null) {
            Stream(id = get.stream?.id, user_id = channelId, user_login = get.login, user_name = get.displayName,
                game_id = get.stream?.game?.id, game_name = get.stream?.game?.displayName, type = get.stream?.type,
                title = get.stream?.title, viewer_count = get.stream?.viewersCount, started_at = get.stream?.createdAt,
                thumbnail_url = get.stream?.previewImageURL, profileImageURL = get.profileImageURL)
        } else null
    }

    override suspend fun loadVideoGQL(clientId: String?, videoId: String): Video? = withContext(Dispatchers.IO) {
        val get = apolloClient(XtraModule(), clientId).query(VideoQuery(Optional.Present(videoId))).execute().data
        if (get != null) {
            Video(id = get.video?.id ?: "", user_id = get.video?.owner?.id, user_login = get.video?.owner?.login, user_name = get.video?.owner?.displayName,
                profileImageURL = get.video?.owner?.profileImageURL)
        } else null
    }

    override suspend fun loadUserByIdGQL(clientId: String?, channelId: String): User? = withContext(Dispatchers.IO) {
        val get = apolloClient(XtraModule(), clientId).query(UserQuery(Optional.Present(channelId))).execute().data
        if (get != null) {
            User(id = channelId, login = get.user?.login, display_name = get.user?.displayName, profile_image_url = get.user?.profileImageURL)
        } else null
    }

    override suspend fun loadCheerEmotesGQL(clientId: String?, userId: String): List<CheerEmote>? = withContext(Dispatchers.IO) {
        val emotes = mutableListOf<CheerEmote>()
        val get = apolloClient(XtraModule(), clientId).query(CheerEmotesQuery(Optional.Present(userId), Optional.Present(animateGifs), Optional.Present((when (emoteQuality) {4 -> 4 3 -> 3 2 -> 2 else -> 1}).toDouble()))).execute().data
        if (get?.user?.cheer?.emotes != null) {
            for (i in get.user.cheer.emotes) {
                if (i?.tiers != null) {
                    for (tier in i.tiers) {
                        i.prefix?.let { tier?.bits?.let { it1 -> tier.images?.first()?.url?.let { it2 -> emotes.add(CheerEmote(name = it, minBits = it1, color = tier.color, type = if (animateGifs) "image/gif" else "image/png", url = it2)) } } }
                    }
                }
            }
        }
        if (emotes.isNotEmpty()) {
            emotes
        } else null
    }

    override fun loadTopGamesGQL(clientId: String?, coroutineScope: CoroutineScope): Listing<Game> {
        val factory = GamesDataSourceGQLquery.Factory(clientId, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(30)
            .setInitialLoadSizeHint(30)
            .setPrefetchDistance(10)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadTopStreamsGQL(clientId: String?, thumbnailsEnabled: Boolean, coroutineScope: CoroutineScope): Listing<Stream> {
        val factory = StreamsDataSourceGQLquery.Factory(clientId, coroutineScope)
        val builder = PagedList.Config.Builder().setEnablePlaceholders(false)
        if (thumbnailsEnabled) {
            builder.setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
        } else {
            builder.setPageSize(30)
                .setInitialLoadSizeHint(30)
                .setPrefetchDistance(10)
        }
        val config = builder.build()
        return Listing.create(factory, config)
    }

    override fun loadTopVideosGQL(clientId: String?, coroutineScope: CoroutineScope): Listing<Video> {
        val factory = VideosDataSourceGQLquery.Factory(clientId, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadGameStreamsGQL(clientId: String?, gameId: String?, coroutineScope: CoroutineScope): Listing<Stream> {
        val factory = GameStreamsDataSourceGQLquery.Factory(clientId, gameId, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadGameVideosGQL(clientId: String?, gameId: String?, type: com.github.andreyasadchy.xtra.type.BroadcastType?, sort: VideoSort?, coroutineScope: CoroutineScope): Listing<Video> {
        val factory = GameVideosDataSourceGQLquery.Factory(clientId, gameId, type, sort, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadGameClipsGQL(clientId: String?, gameId: String?, sort: ClipsPeriod?, coroutineScope: CoroutineScope): Listing<Clip> {
        val factory = GameClipsDataSourceGQLquery.Factory(clientId, gameId, sort, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadChannelVideosGQL(clientId: String?, channelId: String?, type: com.github.andreyasadchy.xtra.type.BroadcastType?, sort: VideoSort?, coroutineScope: CoroutineScope): Listing<Video> {
        val factory = ChannelVideosDataSourceGQLquery.Factory(clientId, channelId, type, sort, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadChannelClipsGQL(clientId: String?, channelId: String?, sort: ClipsPeriod?, coroutineScope: CoroutineScope): Listing<Clip> {
        val factory = ChannelClipsDataSourceGQLquery.Factory(clientId, channelId, sort, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadSearchChannelsGQL(clientId: String?, query: String, coroutineScope: CoroutineScope): Listing<ChannelSearch> {
        val factory = SearchChannelsDataSourceGQL.Factory(clientId, query, gql, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(15)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(5)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadSearchGamesGQL(clientId: String?, query: String, coroutineScope: CoroutineScope): Listing<Game> {
        val factory = SearchGamesDataSourceGQL.Factory(clientId, query, gql, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(15)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(5)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }
}