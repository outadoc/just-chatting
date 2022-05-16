package com.github.andreyasadchy.xtra.repository

import androidx.core.util.Pair
import com.github.andreyasadchy.xtra.model.chat.CheerEmote
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.model.chat.VideoMessagesResponse
import com.github.andreyasadchy.xtra.model.helix.channel.ChannelSearch
import com.github.andreyasadchy.xtra.model.helix.channel.ChannelViewerList
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.model.helix.follows.Order
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.model.helix.tag.Tag
import com.github.andreyasadchy.xtra.model.helix.user.User
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.type.ClipsPeriod
import com.github.andreyasadchy.xtra.type.Language
import com.github.andreyasadchy.xtra.type.VideoSort
import kotlinx.coroutines.CoroutineScope

interface TwitchService {

    fun loadTopGames(helixClientId: String?, helixToken: String?, gqlClientId: String?, tags: List<String>?, apiPref: ArrayList<Pair<Long?, String?>?>, coroutineScope: CoroutineScope): Listing<Game>
    fun loadTopStreams(helixClientId: String?, helixToken: String?, gqlClientId: String?, tags: List<String>?, apiPref: ArrayList<Pair<Long?, String?>?>, thumbnailsEnabled: Boolean, coroutineScope: CoroutineScope): Listing<Stream>
    fun loadGameStreams(gameId: String?, gameName: String?, helixClientId: String?, helixToken: String?, gqlClientId: String?, tags: List<String>?, apiPref: ArrayList<Pair<Long?, String?>?>, thumbnailsEnabled: Boolean, coroutineScope: CoroutineScope): Listing<Stream>
    fun loadGameVideos(gameId: String?, gameName: String?, helixClientId: String?, helixToken: String?, helixPeriod: Period, helixBroadcastTypes: BroadcastType, helixLanguage: String?, helixSort: Sort, gqlClientId: String?, gqlQueryLanguages: List<String>?, gqlQueryType: com.github.andreyasadchy.xtra.type.BroadcastType?, gqlQuerySort: VideoSort?, gqlType: String?, gqlSort: String?, apiPref: ArrayList<Pair<Long?, String?>?>, coroutineScope: CoroutineScope): Listing<Video>
    fun loadGameClips(gameId: String?, gameName: String?, helixClientId: String?, helixToken: String?, started_at: String?, ended_at: String?, gqlClientId: String?, gqlQueryLanguages: List<Language>?, gqlQueryPeriod: ClipsPeriod?, gqlPeriod: String?, apiPref: ArrayList<Pair<Long?, String?>?>, coroutineScope: CoroutineScope): Listing<Clip>
    fun loadChannelVideos(channelId: String?, channelLogin: String?, helixClientId: String?, helixToken: String?, helixPeriod: Period, helixBroadcastTypes: BroadcastType, helixSort: Sort, gqlClientId: String?, gqlQueryType: com.github.andreyasadchy.xtra.type.BroadcastType?, gqlQuerySort: VideoSort?, gqlType: String?, gqlSort: String?, apiPref: ArrayList<Pair<Long?, String?>?>, coroutineScope: CoroutineScope): Listing<Video>
    fun loadChannelClips(channelId: String?, channelLogin: String?, helixClientId: String?, helixToken: String?, started_at: String?, ended_at: String?, gqlClientId: String?, gqlQueryPeriod: ClipsPeriod?, gqlPeriod: String?, apiPref: ArrayList<Pair<Long?, String?>?>, coroutineScope: CoroutineScope): Listing<Clip>
    fun loadSearchChannels(query: String, helixClientId: String?, helixToken: String?, gqlClientId: String?, apiPref: ArrayList<Pair<Long?, String?>?>?, coroutineScope: CoroutineScope): Listing<ChannelSearch>
    fun loadSearchGames(query: String, helixClientId: String?, helixToken: String?, gqlClientId: String?, apiPref: ArrayList<Pair<Long?, String?>?>?, coroutineScope: CoroutineScope): Listing<Game>
    fun loadFollowedStreams(userId: String?, helixClientId: String?, helixToken: String?, gqlClientId: String?, gqlToken: String?, apiPref: ArrayList<Pair<Long?, String?>?>, thumbnailsEnabled: Boolean, coroutineScope: CoroutineScope): Listing<Stream>
    fun loadFollowedVideos(userId: String?, gqlClientId: String?, gqlToken: String?, gqlQueryType: com.github.andreyasadchy.xtra.type.BroadcastType?, gqlQuerySort: VideoSort?, apiPref: ArrayList<Pair<Long?, String?>?>, coroutineScope: CoroutineScope): Listing<Video>
    fun loadFollowedChannels(userId: String?, helixClientId: String?, helixToken: String?, gqlClientId: String?, gqlToken: String?, apiPref: ArrayList<Pair<Long?, String?>?>, sort: com.github.andreyasadchy.xtra.model.helix.follows.Sort, order: Order, coroutineScope: CoroutineScope): Listing<Follow>
    fun loadFollowedGames(userId: String?, gqlClientId: String?, gqlToken: String?, apiPref: ArrayList<Pair<Long?, String?>?>, coroutineScope: CoroutineScope): Listing<Game>
    suspend fun loadGameBoxArt(gameId: String, helixClientId: String?, helixToken: String?, gqlClientId: String?): String?
    suspend fun loadStream(channelId: String, channelLogin: String?, helixClientId: String?, helixToken: String?, gqlClientId: String?) : Stream?
    suspend fun loadStreamWithUser(channelId: String, helixClientId: String?, helixToken: String?, gqlClientId: String?): Stream?
    suspend fun loadVideo(videoId: String, helixClientId: String?, helixToken: String?, gqlClientId: String?): Video?
    suspend fun loadVideos(ids: List<String>, helixClientId: String?, helixToken: String?): List<Video>?
    suspend fun loadClip(clipId: String, helixClientId: String?, helixToken: String?, gqlClientId: String?): Clip?
    suspend fun loadUsersById(ids: List<String>, helixClientId: String?, helixToken: String?, gqlClientId: String?): List<User>?
    suspend fun loadUsersByLogin(logins: List<String>, helixClientId: String?, helixToken: String?, gqlClientId: String?): List<User>?
    suspend fun loadUserTypes(ids: List<String>, helixClientId: String?, helixToken: String?, gqlClientId: String?): List<User>?
    suspend fun loadCheerEmotes(userId: String, helixClientId: String?, helixToken: String?, gqlClientId: String?): List<CheerEmote>?
    suspend fun loadEmotesFromSet(helixClientId: String?, helixToken: String?, setIds: List<String>): List<TwitchEmote>?
    suspend fun loadUserFollowing(helixClientId: String?, helixToken: String?, userId: String?, channelId: String?, gqlClientId: String?, gqlToken: String?, userLogin: String?): Boolean
    suspend fun loadGameFollowing(gqlClientId: String?, gqlToken: String?, gameName: String?): Boolean
    suspend fun loadVideoChatLog(gqlClientId: String?, videoId: String, offsetSeconds: Double): VideoMessagesResponse
    suspend fun loadVideoChatAfter(gqlClientId: String?, videoId: String, cursor: String): VideoMessagesResponse
    suspend fun loadVodGamesGQL(clientId: String?, videoId: String?): List<Game>?
    suspend fun loadChannelViewerListGQL(clientId: String?, channelLogin: String?): ChannelViewerList
    suspend fun followUser(gqlClientId: String?, gqlToken: String?, userId: String?): Boolean
    suspend fun unfollowUser(gqlClientId: String?, gqlToken: String?, userId: String?): Boolean
    suspend fun followGame(gqlClientId: String?, gqlToken: String?, gameId: String?): Boolean
    suspend fun unfollowGame(gqlClientId: String?, gqlToken: String?, gameId: String?): Boolean
    fun loadTagsGQL(clientId: String?, getGameTags: Boolean, gameId: String?, gameName: String?, query: String?, coroutineScope: CoroutineScope): Listing<Tag>
}
