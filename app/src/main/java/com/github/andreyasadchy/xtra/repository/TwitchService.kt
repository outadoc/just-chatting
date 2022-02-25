package com.github.andreyasadchy.xtra.repository

import com.github.andreyasadchy.xtra.model.chat.CheerEmote
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.model.chat.VideoMessagesResponse
import com.github.andreyasadchy.xtra.model.helix.channel.ChannelSearch
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.model.helix.follows.Order
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.model.helix.tag.Tag
import com.github.andreyasadchy.xtra.model.helix.user.User
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.type.ClipsPeriod
import com.github.andreyasadchy.xtra.type.Language
import com.github.andreyasadchy.xtra.type.VideoSort
import kotlinx.coroutines.CoroutineScope

interface TwitchService {

    fun loadTopGames(clientId: String?, userToken: String?, coroutineScope: CoroutineScope): Listing<Game>
    suspend fun loadStream(clientId: String?, userToken: String?, channelId: String): Stream?
    fun loadTopStreams(clientId: String?, userToken: String?, gameId: String?, thumbnailsEnabled: Boolean, coroutineScope: CoroutineScope): Listing<Stream>
    fun loadFollowedStreams(useHelix: Boolean, gqlClientId: String?, helixClientId: String?, userToken: String?, userId: String, thumbnailsEnabled: Boolean, coroutineScope: CoroutineScope): Listing<Stream>
    fun loadClips(clientId: String?, userToken: String?, channelId: String?, channelLogin: String?, gameId: String?, started_at: String?, ended_at: String?, coroutineScope: CoroutineScope): Listing<Clip>
    suspend fun loadVideo(clientId: String?, userToken: String?, videoId: String): Video?
    fun loadVideos(clientId: String?, userToken: String?, gameId: String?, period: com.github.andreyasadchy.xtra.model.helix.video.Period, broadcastType: BroadcastType, language: String?, sort: Sort, coroutineScope: CoroutineScope): Listing<Video>
    fun loadChannelVideos(clientId: String?, userToken: String?, channelId: String, period: com.github.andreyasadchy.xtra.model.helix.video.Period, broadcastType: BroadcastType, sort: Sort, coroutineScope: CoroutineScope): Listing<Video>
    suspend fun loadUserById(clientId: String?, userToken: String?, id: String): User?
    fun loadSearchGames(clientId: String?, userToken: String?, query: String, coroutineScope: CoroutineScope): Listing<Game>
    fun loadSearchChannels(clientId: String?, userToken: String?, query: String, coroutineScope: CoroutineScope): Listing<ChannelSearch>
    suspend fun loadUserFollows(clientId: String?, userToken: String?, userId: String, channelId: String): Boolean
    fun loadFollowedChannels(gqlClientId: String?, helixClientId: String?, userToken: String?, userId: String, sort: com.github.andreyasadchy.xtra.model.helix.follows.Sort, order: Order, coroutineScope: CoroutineScope): Listing<Follow>
    suspend fun loadEmotesFromSet(clientId: String?, userToken: String?, setIds: List<String>): List<TwitchEmote>?
    suspend fun loadCheerEmotes(clientId: String?, userToken: String?, userId: String): List<CheerEmote>?
    suspend fun loadVideoChatLog(clientId: String?, videoId: String, offsetSeconds: Double): VideoMessagesResponse
    suspend fun loadVideoChatAfter(clientId: String?, videoId: String, cursor: String): VideoMessagesResponse

    suspend fun loadStreamGQLQuery(clientId: String?, channelId: String): Stream?
    suspend fun loadVideoGQLQuery(clientId: String?, videoId: String): Video?
    suspend fun loadUserByIdGQLQuery(clientId: String?, channelId: String): User?
    suspend fun loadStreamWithUserGQLQuery(clientId: String?, channelId: String): Stream?
    suspend fun loadCheerEmotesGQLQuery(clientId: String?, userId: String): List<CheerEmote>?
    fun loadTopGamesGQLQuery(clientId: String?, coroutineScope: CoroutineScope): Listing<Game>
    fun loadTopStreamsGQLQuery(clientId: String?, thumbnailsEnabled: Boolean, coroutineScope: CoroutineScope): Listing<Stream>
    fun loadTopVideosGQLQuery(clientId: String?, coroutineScope: CoroutineScope): Listing<Video>
    fun loadGameStreamsGQLQuery(clientId: String?, gameId: String?, languages: List<String>?, coroutineScope: CoroutineScope): Listing<Stream>
    fun loadGameVideosGQLQuery(clientId: String?, gameId: String?, languages: List<String>?, type: com.github.andreyasadchy.xtra.type.BroadcastType?, sort: VideoSort?, coroutineScope: CoroutineScope): Listing<Video>
    fun loadGameClipsGQLQuery(clientId: String?, gameId: String?, languages: List<Language>?, sort: ClipsPeriod?, coroutineScope: CoroutineScope): Listing<Clip>
    fun loadChannelVideosGQLQuery(clientId: String?, channelId: String?, type: com.github.andreyasadchy.xtra.type.BroadcastType?, sort: VideoSort?, coroutineScope: CoroutineScope): Listing<Video>
    fun loadChannelClipsGQLQuery(clientId: String?, channelId: String?, sort: ClipsPeriod?, coroutineScope: CoroutineScope): Listing<Clip>

    suspend fun loadVodGamesGQL(clientId: String?, videoId: String?): List<Game>?
    fun loadTagsGQL(clientId: String?, getGameTags: Boolean, gameId: String?, gameName: String?, query: String?, coroutineScope: CoroutineScope): Listing<Tag>
    fun loadTopGamesGQL(clientId: String?, tags: List<String>?, coroutineScope: CoroutineScope): Listing<Game>
    fun loadTopStreamsGQL(clientId: String?, tags: List<String>?, thumbnailsEnabled: Boolean, coroutineScope: CoroutineScope): Listing<Stream>
    fun loadGameStreamsGQL(clientId: String?, gameName: String?, tags: List<String>?, coroutineScope: CoroutineScope): Listing<Stream>
    fun loadSearchChannelsGQL(clientId: String?, query: String, coroutineScope: CoroutineScope): Listing<ChannelSearch>
    fun loadSearchGamesGQL(clientId: String?, query: String, coroutineScope: CoroutineScope): Listing<Game>
}
