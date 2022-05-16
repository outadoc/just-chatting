package com.github.andreyasadchy.xtra.repository

import androidx.core.util.Pair
import androidx.paging.PagedList
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.*
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.api.MiscApi
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
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
import com.github.andreyasadchy.xtra.repository.datasource.*
import com.github.andreyasadchy.xtra.type.ClipsPeriod
import com.github.andreyasadchy.xtra.type.Language
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
    private val helix: HelixApi,
    private val gql: GraphQLRepository,
    private val misc: MiscApi,
    private val localFollowsChannel: LocalFollowChannelRepository,
    private val localFollowsGame: LocalFollowGameRepository,
    private val offlineRepository: OfflineRepository,
    private val bookmarksRepository: BookmarksRepository) : TwitchService {

    override fun loadTopGames(helixClientId: String?, helixToken: String?, gqlClientId: String?, tags: List<String>?, apiPref: ArrayList<Pair<Long?, String?>?>, coroutineScope: CoroutineScope): Listing<Game> {
        val factory = GamesDataSource.Factory(helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, helix, gqlClientId, tags, gql, apiPref, coroutineScope)
        val config = PagedList.Config.Builder()
                .setPageSize(30)
                .setInitialLoadSizeHint(30)
                .setPrefetchDistance(10)
                .setEnablePlaceholders(false)
                .build()
        return Listing.create(factory, config)
    }

    override fun loadTopStreams(helixClientId: String?, helixToken: String?, gqlClientId: String?, tags: List<String>?, apiPref: ArrayList<Pair<Long?, String?>?>, thumbnailsEnabled: Boolean, coroutineScope: CoroutineScope): Listing<Stream> {
        val factory = StreamsDataSource.Factory(helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, helix, gqlClientId, tags, gql, apiPref, coroutineScope)
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

    override fun loadGameStreams(gameId: String?, gameName: String?, helixClientId: String?, helixToken: String?, gqlClientId: String?, tags: List<String>?, apiPref: ArrayList<Pair<Long?, String?>?>, thumbnailsEnabled: Boolean, coroutineScope: CoroutineScope): Listing<Stream> {
        val factory = GameStreamsDataSource.Factory(gameId, gameName, helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, helix, gqlClientId, tags, gql, apiPref, coroutineScope)
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

    override fun loadGameVideos(gameId: String?, gameName: String?, helixClientId: String?, helixToken: String?, helixPeriod: Period, helixBroadcastTypes: BroadcastType, helixLanguage: String?, helixSort: Sort, gqlClientId: String?, gqlQueryLanguages: List<String>?, gqlQueryType: com.github.andreyasadchy.xtra.type.BroadcastType?, gqlQuerySort: VideoSort?, gqlType: String?, gqlSort: String?, apiPref: ArrayList<Pair<Long?, String?>?>, coroutineScope: CoroutineScope): Listing<Video> {
        val factory = GameVideosDataSource.Factory(gameId, gameName, helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, helixPeriod, helixBroadcastTypes, helixLanguage, helixSort, helix, gqlClientId, gqlQueryLanguages, gqlQueryType, gqlQuerySort, gqlType, gqlSort, gql, apiPref, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadGameClips(gameId: String?, gameName: String?, helixClientId: String?, helixToken: String?, started_at: String?, ended_at: String?, gqlClientId: String?, gqlQueryLanguages: List<Language>?, gqlQueryPeriod: ClipsPeriod?, gqlPeriod: String?, apiPref: ArrayList<Pair<Long?, String?>?>, coroutineScope: CoroutineScope): Listing<Clip> {
        val factory = GameClipsDataSource.Factory(gameId, gameName, helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, started_at, ended_at, helix, gqlClientId, gqlQueryLanguages, gqlQueryPeriod, gqlPeriod, gql, apiPref, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadChannelVideos(channelId: String?, channelLogin: String?, helixClientId: String?, helixToken: String?, helixPeriod: Period, helixBroadcastTypes: BroadcastType, helixSort: Sort, gqlClientId: String?, gqlQueryType: com.github.andreyasadchy.xtra.type.BroadcastType?, gqlQuerySort: VideoSort?, gqlType: String?, gqlSort: String?, apiPref: ArrayList<Pair<Long?, String?>?>, coroutineScope: CoroutineScope): Listing<Video> {
        val factory = ChannelVideosDataSource.Factory(channelId, channelLogin, helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, helixPeriod, helixBroadcastTypes, helixSort, helix, gqlClientId, gqlQueryType, gqlQuerySort, gqlType, gqlSort, gql, apiPref, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadChannelClips(channelId: String?, channelLogin: String?, helixClientId: String?, helixToken: String?, started_at: String?, ended_at: String?, gqlClientId: String?, gqlQueryPeriod: ClipsPeriod?, gqlPeriod: String?, apiPref: ArrayList<Pair<Long?, String?>?>, coroutineScope: CoroutineScope): Listing<Clip> {
        val factory = ChannelClipsDataSource.Factory(channelId, channelLogin, helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, started_at, ended_at, helix, gqlClientId, gqlQueryPeriod, gqlPeriod, gql, apiPref, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadSearchChannels(query: String, helixClientId: String?, helixToken: String?, gqlClientId: String?, apiPref: ArrayList<Pair<Long?, String?>?>?, coroutineScope: CoroutineScope): Listing<ChannelSearch> {
        val factory = SearchChannelsDataSource.Factory(query, helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, helix, gqlClientId, gql, apiPref, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(15)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(5)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadSearchGames(query: String, helixClientId: String?, helixToken: String?, gqlClientId: String?, apiPref: ArrayList<Pair<Long?, String?>?>?, coroutineScope: CoroutineScope): Listing<Game> {
        val factory = SearchGamesDataSource.Factory(query, helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, helix, gqlClientId, gql, apiPref, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(15)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(5)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadFollowedStreams(userId: String?, helixClientId: String?, helixToken: String?, gqlClientId: String?, gqlToken: String?, apiPref: ArrayList<Pair<Long?, String?>?>, thumbnailsEnabled: Boolean, coroutineScope: CoroutineScope): Listing<Stream> {
        val factory = FollowedStreamsDataSource.Factory(localFollowsChannel, userId, helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, helix, gqlClientId, gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) }, gql, apiPref, coroutineScope)
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

    override fun loadFollowedVideos(userId: String?, gqlClientId: String?, gqlToken: String?, gqlQueryType: com.github.andreyasadchy.xtra.type.BroadcastType?, gqlQuerySort: VideoSort?, apiPref: ArrayList<Pair<Long?, String?>?>, coroutineScope: CoroutineScope): Listing<Video> {
        val factory = FollowedVideosDataSource.Factory(userId, gqlClientId, gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) }, gqlQueryType, gqlQuerySort, gql, apiPref, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(3)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadFollowedChannels(userId: String?, helixClientId: String?, helixToken: String?, gqlClientId: String?, gqlToken: String?, apiPref: ArrayList<Pair<Long?, String?>?>, sort: com.github.andreyasadchy.xtra.model.helix.follows.Sort, order: Order, coroutineScope: CoroutineScope): Listing<Follow> {
        val factory = FollowedChannelsDataSource.Factory(localFollowsChannel, offlineRepository, bookmarksRepository, userId, helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, helix, gqlClientId, gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) }, gql, apiPref, sort, order, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(40)
            .setInitialLoadSizeHint(40)
            .setPrefetchDistance(10)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadFollowedGames(userId: String?, gqlClientId: String?, gqlToken: String?, apiPref: ArrayList<Pair<Long?, String?>?>, coroutineScope: CoroutineScope): Listing<Game> {
        val factory = FollowedGamesDataSource.Factory(localFollowsGame, userId, gqlClientId, gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) }, gql, apiPref, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(40)
            .setInitialLoadSizeHint(40)
            .setPrefetchDistance(10)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadGameBoxArt(gameId: String, helixClientId: String?, helixToken: String?, gqlClientId: String?): String? = withContext(Dispatchers.IO) {
        try {
            apolloClient(XtraModule(), gqlClientId).query(GameBoxArtQuery(Optional.Present(gameId))).execute().data?.game?.boxArtURL
        } catch (e: Exception) {
            helix.getGames(helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, mutableListOf(gameId)).data?.firstOrNull()?.boxArt
        }
    }

    override suspend fun loadStream(channelId: String, channelLogin: String?, helixClientId: String?, helixToken: String?, gqlClientId: String?): Stream? = withContext(Dispatchers.IO) {
        try {
            val get = apolloClient(XtraModule(), gqlClientId).query(StreamsQuery(Optional.Present(mutableListOf(channelId)))).execute().data?.users?.firstOrNull()
            if (get != null) {
                Stream(id = get.stream?.id, user_id = channelId, user_login = get.login, user_name = get.displayName,
                    game_id = get.stream?.game?.id, game_name = get.stream?.game?.displayName, type = get.stream?.type,
                    title = get.stream?.title, viewer_count = get.stream?.viewersCount, started_at = get.stream?.createdAt,
                    thumbnail_url = get.stream?.previewImageURL, profileImageURL = get.profileImageURL)
            } else null
        } catch (e: Exception) {
            try {
                helix.getStreams(helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, mutableListOf(channelId)).data?.firstOrNull()
            } catch (e: Exception) {
                Stream(viewer_count = gql.loadViewerCount(gqlClientId, channelLogin).viewers)
            }
        }
    }

    override suspend fun loadStreamWithUser(channelId: String, helixClientId: String?, helixToken: String?, gqlClientId: String?): Stream? = withContext(Dispatchers.IO) {
        try {
            val userIds = mutableListOf<String>()
            userIds.add(channelId)
            val get = apolloClient(XtraModule(), gqlClientId).query(StreamUserQuery(Optional.Present(userIds))).execute().data
            if (get != null) {
                val user = User(id = channelId, login = get.users?.first()?.login, display_name = get.users?.first()?.displayName, profile_image_url = get.users?.first()?.profileImageURL,
                    bannerImageURL = get.users?.first()?.bannerImageURL, view_count = get.users?.first()?.profileViewCount, created_at = get.users?.first()?.createdAt?.toString(),
                    followers_count = get.users?.first()?.followers?.totalCount,
                    broadcaster_type = when {
                        get.users?.first()?.roles?.isPartner == true -> "partner"
                        get.users?.first()?.roles?.isAffiliate == true -> "affiliate"
                        else -> null
                    },
                    type = when {
                        get.users?.first()?.roles?.isStaff == true -> "staff"
                        get.users?.first()?.roles?.isSiteAdmin == true -> "admin"
                        get.users?.first()?.roles?.isGlobalMod == true -> "global_mod"
                        else -> null
                    }
                )
                Stream(id = get.users?.first()?.stream?.id, user_id = channelId, user_login = get.users?.first()?.login, user_name = get.users?.first()?.displayName,
                    game_id = get.users?.first()?.stream?.game?.id, game_name = get.users?.first()?.stream?.game?.displayName, type = get.users?.first()?.stream?.type,
                    title = get.users?.first()?.stream?.title, viewer_count = get.users?.first()?.stream?.viewersCount, started_at = get.users?.first()?.stream?.createdAt,
                    thumbnail_url = get.users?.first()?.stream?.previewImageURL, profileImageURL = get.users?.first()?.profileImageURL, channelUser = user,
                    lastBroadcast = get.users?.first()?.lastBroadcast?.startedAt?.toString())
            } else null
        } catch (e: Exception) {
            helix.getStreams(helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, mutableListOf(channelId)).data?.firstOrNull()
        }
    }

    override suspend fun loadVideo(videoId: String, helixClientId: String?, helixToken: String?, gqlClientId: String?): Video? = withContext(Dispatchers.IO) {
        try {
            val get = apolloClient(XtraModule(), gqlClientId).query(VideoQuery(Optional.Present(videoId))).execute().data
            if (get != null) {
                Video(id = videoId, user_id = get.video?.owner?.id, user_login = get.video?.owner?.login, user_name = get.video?.owner?.displayName,
                    profileImageURL = get.video?.owner?.profileImageURL, title = get.video?.title, createdAt = get.video?.createdAt, thumbnail_url = get.video?.previewThumbnailURL,
                    type = get.video?.broadcastType.toString(), duration = get.video?.lengthSeconds.toString())
            } else null
        } catch (e: Exception) {
            helix.getVideos(helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, mutableListOf(videoId)).data?.firstOrNull()
        }
    }

    override suspend fun loadVideos(ids: List<String>, helixClientId: String?, helixToken: String?): List<Video>? = withContext(Dispatchers.IO) {
        helix.getVideos(helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, ids).data
    }

    override suspend fun loadClip(clipId: String, helixClientId: String?, helixToken: String?, gqlClientId: String?): Clip? = withContext(Dispatchers.IO) {
        try {
            var user: Clip? = null
            try {
                user = gql.loadClipData(gqlClientId, clipId).data
            } catch (e: Exception) {}
            val video = gql.loadClipVideo(gqlClientId, clipId).data
            Clip(id = clipId, broadcaster_id = user?.broadcaster_id, broadcaster_login = user?.broadcaster_login, broadcaster_name = user?.broadcaster_name,
                profileImageURL = user?.profileImageURL, video_id = video?.video_id, duration = video?.duration, videoOffsetSeconds = video?.videoOffsetSeconds ?: user?.videoOffsetSeconds)
        } catch (e: Exception) {
            helix.getClips(helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, mutableListOf(clipId)).data?.firstOrNull()
        }
    }

    override suspend fun loadUsersById(ids: List<String>, helixClientId: String?, helixToken: String?, gqlClientId: String?): List<User>? = withContext(Dispatchers.IO) {
        try {
            val get = apolloClient(XtraModule(), gqlClientId).query(UsersQuery(ids = Optional.Present(ids))).execute().data?.users
            if (get != null) {
                val list = mutableListOf<User>()
                for (i in get) {
                    list.add(
                        User(id = i?.id, login = i?.login, display_name = i?.displayName, profile_image_url = i?.profileImageURL,
                            bannerImageURL = i?.bannerImageURL, view_count = i?.profileViewCount, created_at = i?.createdAt?.toString(),
                            followers_count = i?.followers?.totalCount,
                            broadcaster_type = when {
                                i?.roles?.isPartner == true -> "partner"
                                i?.roles?.isAffiliate == true -> "affiliate"
                                else -> null
                            },
                            type = when {
                                i?.roles?.isStaff == true -> "staff"
                                i?.roles?.isSiteAdmin == true -> "admin"
                                i?.roles?.isGlobalMod == true -> "global_mod"
                                else -> null
                            }))
                }
                list
            } else null
        } catch (e: Exception) {
            helix.getUsersById(helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, ids).data
        }
    }

    override suspend fun loadUsersByLogin(logins: List<String>, helixClientId: String?, helixToken: String?, gqlClientId: String?): List<User>? = withContext(Dispatchers.IO) {
        try {
            val get = apolloClient(XtraModule(), gqlClientId).query(UsersQuery(logins = Optional.Present(logins))).execute().data?.users
            if (get != null) {
                val list = mutableListOf<User>()
                for (i in get) {
                    list.add(
                        User(id = i?.id, login = i?.login, display_name = i?.displayName, profile_image_url = i?.profileImageURL,
                            bannerImageURL = i?.bannerImageURL, view_count = i?.profileViewCount, created_at = i?.createdAt?.toString(),
                            followers_count = i?.followers?.totalCount,
                            broadcaster_type = when {
                                i?.roles?.isPartner == true -> "partner"
                                i?.roles?.isAffiliate == true -> "affiliate"
                                else -> null
                            },
                            type = when {
                                i?.roles?.isStaff == true -> "staff"
                                i?.roles?.isSiteAdmin == true -> "admin"
                                i?.roles?.isGlobalMod == true -> "global_mod"
                                else -> null
                            }))
                }
                list
            } else null
        } catch (e: Exception) {
            helix.getUsersByLogin(helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, logins).data
        }
    }

    override suspend fun loadUserTypes(ids: List<String>, helixClientId: String?, helixToken: String?, gqlClientId: String?): List<User>? = withContext(Dispatchers.IO) {
        try {
            val get = apolloClient(XtraModule(), gqlClientId).query(UserTypeQuery(Optional.Present(ids))).execute().data?.users
            if (get != null) {
                val list = mutableListOf<User>()
                for (i in get) {
                    list.add(
                        User(id = i?.id,
                            broadcaster_type = when {
                                i?.roles?.isPartner == true -> "partner"
                                i?.roles?.isAffiliate == true -> "affiliate"
                                else -> null
                            },
                            type = when {
                                i?.roles?.isStaff == true -> "staff"
                                i?.roles?.isSiteAdmin == true -> "admin"
                                i?.roles?.isGlobalMod == true -> "global_mod"
                                else -> null
                            }))
                }
                list
            } else null
        } catch (e: Exception) {
            helix.getUsersById(helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, ids).data
        }
    }

    override suspend fun loadCheerEmotes(userId: String, helixClientId: String?, helixToken: String?, gqlClientId: String?): List<CheerEmote>? = withContext(Dispatchers.IO) {
        try {
            val emotes = mutableListOf<CheerEmote>()
            val get = apolloClient(XtraModule(), gqlClientId).query(CheerEmotesQuery(Optional.Present(userId), Optional.Present(animateGifs), Optional.Present((when (emoteQuality) {4 -> 4 3 -> 3 2 -> 2 else -> 1}).toDouble()))).execute().data
            if (get?.user?.cheer?.emotes != null) {
                for (i in get.user.cheer.emotes) {
                    if (i?.tiers != null) {
                        for (tier in i.tiers) {
                            i.prefix?.let { tier?.bits?.let { it1 -> tier.images?.first()?.url?.let { it2 -> emotes.add(CheerEmote(name = it, minBits = it1, color = tier.color, type = if (animateGifs) "image/gif" else "image/png", url = it2)) } } }
                        }
                    }
                }
            }
            emotes.ifEmpty { null }
        } catch (e: Exception) {
            helix.getCheerEmotes(helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, userId).emotes
        }
    }

    override suspend fun loadEmotesFromSet(helixClientId: String?, helixToken: String?, setIds: List<String>): List<TwitchEmote>? = withContext(Dispatchers.IO) {
        helix.getEmotesFromSet(helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, setIds).emotes
    }

    override suspend fun loadUserFollowing(helixClientId: String?, helixToken: String?, userId: String?, channelId: String?, gqlClientId: String?, gqlToken: String?, userLogin: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!gqlToken.isNullOrBlank()) gql.loadFollowingUser(gqlClientId, gqlToken.let { TwitchApiHelper.addTokenPrefixGQL(it) }, userLogin).following else throw Exception()
        } catch (e: Exception) {
            helix.getUserFollows(helixClientId, helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) }, userId, channelId).total == 1
        }
    }

    override suspend fun loadGameFollowing(gqlClientId: String?, gqlToken: String?, gameName: String?): Boolean = withContext(Dispatchers.IO) {
        gql.loadFollowingGame(gqlClientId, gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) }, gameName).following
    }

    override suspend fun loadVideoChatLog(gqlClientId: String?, videoId: String, offsetSeconds: Double): VideoMessagesResponse = withContext(Dispatchers.IO) {
        misc.getVideoChatLog(gqlClientId, videoId, offsetSeconds, 100)
    }

    override suspend fun loadVideoChatAfter(gqlClientId: String?, videoId: String, cursor: String): VideoMessagesResponse = withContext(Dispatchers.IO) {
        misc.getVideoChatLogAfter(gqlClientId, videoId, cursor, 100)
    }

    override suspend fun loadVodGamesGQL(clientId: String?, videoId: String?): List<Game> = withContext(Dispatchers.IO) {
        gql.loadVodGames(clientId, videoId).data
    }

    override suspend fun loadChannelViewerListGQL(clientId: String?, channelLogin: String?): ChannelViewerList = withContext(Dispatchers.IO) {
        gql.loadChannelViewerList(clientId, channelLogin).data
    }

    override suspend fun followUser(gqlClientId: String?, gqlToken: String?, userId: String?): Boolean = withContext(Dispatchers.IO) {
        gql.loadFollowUser(gqlClientId, gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) }, userId).error.isNullOrBlank()
    }

    override suspend fun unfollowUser(gqlClientId: String?, gqlToken: String?, userId: String?): Boolean = withContext(Dispatchers.IO) {
        !gql.loadUnfollowUser(gqlClientId, gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) }, userId).isJsonNull
    }

    override suspend fun followGame(gqlClientId: String?, gqlToken: String?, gameId: String?): Boolean = withContext(Dispatchers.IO) {
        !gql.loadFollowGame(gqlClientId, gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) }, gameId).isJsonNull
    }

    override suspend fun unfollowGame(gqlClientId: String?, gqlToken: String?, gameId: String?): Boolean = withContext(Dispatchers.IO) {
        !gql.loadUnfollowGame(gqlClientId, gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) }, gameId).isJsonNull
    }

    override fun loadTagsGQL(clientId: String?, getGameTags: Boolean, gameId: String?, gameName: String?, query: String?, coroutineScope: CoroutineScope): Listing<Tag> {
        val factory = TagsDataSourceGQL.Factory(clientId, getGameTags, gameId, gameName, query, gql, coroutineScope)
        val config = PagedList.Config.Builder()
            .setPageSize(10000)
            .setInitialLoadSizeHint(10000)
            .setPrefetchDistance(5)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }
}