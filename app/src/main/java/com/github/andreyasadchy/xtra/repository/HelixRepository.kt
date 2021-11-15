package com.github.andreyasadchy.xtra.repository

import android.util.Log
import androidx.paging.PagedList
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.db.EmotesDao
import com.github.andreyasadchy.xtra.model.chat.VideoMessagesResponse
import com.github.andreyasadchy.xtra.model.helix.channel.Channel
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.model.helix.stream.StreamsResponse
import com.github.andreyasadchy.xtra.model.helix.user.User
import com.github.andreyasadchy.xtra.model.helix.video.*
import com.github.andreyasadchy.xtra.repository.datasource.*
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HelixRepository"

@Singleton
class HelixRepository @Inject constructor(
    private val api: HelixApi,
    private val emotesDao: EmotesDao) : TwitchService {

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

    override suspend fun loadStream(clientId: String?, userToken: String?, channelId: String): StreamsResponse = withContext(Dispatchers.IO) {
        StreamsResponse(api.getStream(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, channelId, null).data, null)
    }

    override fun loadStreams(clientId: String?, userToken: String?, game: String?, languages: String?, coroutineScope: CoroutineScope): Listing<Stream> {
        val factory = StreamsDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, game, languages, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override fun loadFollowedStreams(clientId: String?, userToken: String?, user_id: String, thumbnailsEnabled: Boolean, coroutineScope: CoroutineScope): Listing<Stream> {
        val factory = FollowedStreamsDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, user_id, api, coroutineScope)
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

    override fun loadClips(clientId: String?, userToken: String?, channelName: String?, gameName: String?, coroutineScope: CoroutineScope): Listing<Clip> {
        val factory = ClipsDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, channelName, gameName, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadVideo(clientId: String?, userToken: String?, videoId: String): VideosResponse = withContext(Dispatchers.IO) {
        VideosResponse(api.getVideo(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, videoId).data, null)
    }

    override fun loadVideos(clientId: String?, userToken: String?, game: String?, period: Period, broadcastType: BroadcastType, language: String?, sort: Sort, coroutineScope: CoroutineScope): Listing<Video> {
        val factory = VideosDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, game, period, broadcastType, language, sort, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override fun loadChannelVideos(clientId: String?, userToken: String?, channelId: String, broadcastType: BroadcastType, sort: Sort, coroutineScope: CoroutineScope): Listing<Video> {
        val factory = ChannelVideosDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, channelId, broadcastType, sort, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(15)
                .setPrefetchDistance(3)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadUserById(clientId: String?, userToken: String?, id: String): User = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading user by id $id")
        api.getUserById(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, id).data?.first()!!
    }

    override suspend fun loadUserByLogin(clientId: String?, userToken: String?, login: String): User = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading user by login $login")
        api.getUsersByLogin(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, login).data?.first()!!
    }

    override suspend fun loadGames(clientId: String?, userToken: String?, query: String): List<Game> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading games containing: $query")
        api.getGames(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, query).data ?: emptyList()
    }

    override fun loadChannels(clientId: String?, userToken: String?, query: String, coroutineScope: CoroutineScope): Listing<Channel> {
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

    override suspend fun loadVideoChatLog(videoId: String, offsetSeconds: Double): VideoMessagesResponse = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading chat log for video $videoId. Offset in seconds: $offsetSeconds")
        api.getVideoChatLog(videoId.substring(1), offsetSeconds, 100)
    }

    override suspend fun loadVideoChatAfter(videoId: String, cursor: String): VideoMessagesResponse = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading chat log for video $videoId. Cursor: $cursor")
        api.getVideoChatLogAfter(videoId.substring(1), cursor, 100)
    }

    override suspend fun loadUserFollows(clientId: String?, userToken: String?, userId: String, channelId: String): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Loading if user is following channel $channelId")
        api.getUserFollows(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, userId, channelId).total == 1
    }

    override fun loadFollowedChannels(clientId: String?, userToken: String?, userId: String, coroutineScope: CoroutineScope): Listing<Follow> {
        val factory = FollowedChannelsDataSource.Factory(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, userId, api, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(40)
                .setInitialLoadSizeHint(40)
                .setPrefetchDistance(10)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }
}