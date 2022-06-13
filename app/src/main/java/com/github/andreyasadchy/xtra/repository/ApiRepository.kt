package com.github.andreyasadchy.xtra.repository

import androidx.core.util.Pair
import androidx.paging.PagedList
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.CheerEmotesQuery
import com.github.andreyasadchy.xtra.GameBoxArtQuery
import com.github.andreyasadchy.xtra.StreamUserQuery
import com.github.andreyasadchy.xtra.UsersQuery
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.chat.CheerEmote
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.model.helix.channel.ChannelSearch
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.model.helix.follows.Order
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.model.helix.user.User
import com.github.andreyasadchy.xtra.repository.datasource.FollowedChannelsDataSource
import com.github.andreyasadchy.xtra.repository.datasource.FollowedStreamsDataSource
import com.github.andreyasadchy.xtra.repository.datasource.SearchChannelsDataSource
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiRepository @Inject constructor(
    private val helix: HelixApi,
    private val gql: GraphQLRepository,
    private val localFollowsChannel: LocalFollowChannelRepository
) : TwitchService {

    override fun loadSearchChannels(
        query: String,
        helixClientId: String?,
        helixToken: String?,
        gqlClientId: String?,
        apiPref: ArrayList<Pair<Long?, String?>?>?,
        coroutineScope: CoroutineScope
    ): Listing<ChannelSearch> {
        val factory = SearchChannelsDataSource.Factory(
            query,
            helixClientId,
            helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) },
            helix,
            gqlClientId,
            gql,
            apiPref,
            coroutineScope
        )
        val config = PagedList.Config.Builder()
            .setPageSize(15)
            .setInitialLoadSizeHint(15)
            .setPrefetchDistance(5)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override fun loadFollowedStreams(
        userId: String?,
        helixClientId: String?,
        helixToken: String?,
        gqlClientId: String?,
        gqlToken: String?,
        apiPref: ArrayList<Pair<Long?, String?>?>,
        thumbnailsEnabled: Boolean,
        coroutineScope: CoroutineScope
    ): Listing<Stream> {
        val factory = FollowedStreamsDataSource.Factory(
            localFollowsChannel = localFollowsChannel,
            userId = userId,
            helixClientId = helixClientId,
            helixToken = helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) },
            helixApi = helix,
            gqlClientId = gqlClientId,
            gqlToken = gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) },
            gqlApi = gql,
            apiPref = apiPref,
            coroutineScope = coroutineScope
        )
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

    override fun loadFollowedChannels(
        userId: String?,
        helixClientId: String?,
        helixToken: String?,
        gqlClientId: String?,
        gqlToken: String?,
        apiPref: ArrayList<Pair<Long?, String?>?>,
        sort: com.github.andreyasadchy.xtra.model.helix.follows.Sort,
        order: Order,
        coroutineScope: CoroutineScope
    ): Listing<Follow> {
        val factory = FollowedChannelsDataSource.Factory(
            localFollowsChannel,
            userId,
            helixClientId,
            helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) },
            helix,
            gqlClientId,
            gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) },
            gql,
            apiPref,
            sort,
            order,
            coroutineScope
        )
        val config = PagedList.Config.Builder()
            .setPageSize(40)
            .setInitialLoadSizeHint(40)
            .setPrefetchDistance(10)
            .setEnablePlaceholders(false)
            .build()
        return Listing.create(factory, config)
    }

    override suspend fun loadGameBoxArt(
        gameId: String,
        helixClientId: String?,
        helixToken: String?,
        gqlClientId: String?
    ): String? = withContext(Dispatchers.IO) {
        try {
            apolloClient(XtraModule(), gqlClientId).query(GameBoxArtQuery(Optional.Present(gameId)))
                .execute().data?.game?.boxArtURL
        } catch (e: Exception) {
            helix.getGames(
                helixClientId,
                helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) },
                mutableListOf(gameId)
            ).data?.firstOrNull()?.boxArt
        }
    }

    override suspend fun loadStreamWithUser(
        channelId: String,
        helixClientId: String?,
        helixToken: String?,
        gqlClientId: String?
    ): Stream? = withContext(Dispatchers.IO) {
        try {
            val userIds = mutableListOf<String>()
            userIds.add(channelId)
            val get = apolloClient(XtraModule(), gqlClientId).query(
                StreamUserQuery(
                    Optional.Present(userIds)
                )
            ).execute().data
            if (get != null) {
                val user = User(
                    id = channelId,
                    login = get.users?.first()?.login,
                    display_name = get.users?.first()?.displayName,
                    profile_image_url = get.users?.first()?.profileImageURL,
                    bannerImageURL = get.users?.first()?.bannerImageURL,
                    view_count = get.users?.first()?.profileViewCount,
                    created_at = get.users?.first()?.createdAt?.toString(),
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
                Stream(
                    id = get.users?.first()?.stream?.id,
                    user_id = channelId,
                    user_login = get.users?.first()?.login,
                    user_name = get.users?.first()?.displayName,
                    game_id = get.users?.first()?.stream?.game?.id,
                    game_name = get.users?.first()?.stream?.game?.displayName,
                    type = get.users?.first()?.stream?.type,
                    title = get.users?.first()?.stream?.title,
                    viewer_count = get.users?.first()?.stream?.viewersCount,
                    started_at = get.users?.first()?.stream?.createdAt,
                    thumbnail_url = get.users?.first()?.stream?.previewImageURL,
                    profileImageURL = get.users?.first()?.profileImageURL,
                    channelUser = user,
                    lastBroadcast = get.users?.first()?.lastBroadcast?.startedAt?.toString()
                )
            } else null
        } catch (e: Exception) {
            helix.getStreams(
                helixClientId,
                helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) },
                mutableListOf(channelId)
            ).data?.firstOrNull()
        }
    }

    override suspend fun loadUsersById(
        ids: List<String>,
        helixClientId: String?,
        helixToken: String?,
        gqlClientId: String?
    ): List<User>? = withContext(Dispatchers.IO) {
        try {
            val get = apolloClient(
                XtraModule(),
                gqlClientId
            ).query(UsersQuery(ids = Optional.Present(ids))).execute().data?.users
            if (get != null) {
                val list = mutableListOf<User>()
                for (i in get) {
                    list.add(
                        User(
                            id = i?.id,
                            login = i?.login,
                            display_name = i?.displayName,
                            profile_image_url = i?.profileImageURL,
                            bannerImageURL = i?.bannerImageURL,
                            view_count = i?.profileViewCount,
                            created_at = i?.createdAt?.toString(),
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
                            }
                        )
                    )
                }
                list
            } else null
        } catch (e: Exception) {
            helix.getUsersById(
                helixClientId,
                helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) },
                ids
            ).data
        }
    }

    override suspend fun loadUsersByLogin(
        logins: List<String>,
        helixClientId: String?,
        helixToken: String?,
        gqlClientId: String?
    ): List<User>? = withContext(Dispatchers.IO) {
        try {
            val get = apolloClient(XtraModule(), gqlClientId).query(
                UsersQuery(
                    logins = Optional.Present(logins)
                )
            ).execute().data?.users
            if (get != null) {
                val list = mutableListOf<User>()
                for (i in get) {
                    list.add(
                        User(
                            id = i?.id,
                            login = i?.login,
                            display_name = i?.displayName,
                            profile_image_url = i?.profileImageURL,
                            bannerImageURL = i?.bannerImageURL,
                            view_count = i?.profileViewCount,
                            created_at = i?.createdAt?.toString(),
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
                            }
                        )
                    )
                }
                list
            } else null
        } catch (e: Exception) {
            helix.getUsersByLogin(
                helixClientId,
                helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) },
                logins
            ).data
        }
    }

    override suspend fun loadCheerEmotes(
        userId: String,
        helixClientId: String?,
        helixToken: String?,
        gqlClientId: String?
    ): List<CheerEmote> = withContext(Dispatchers.IO) {
        try {
            val get = apolloClient(XtraModule(), gqlClientId)
                .query(CheerEmotesQuery(Optional.Present(userId)))
                .execute()
                .data

            get?.user?.cheer?.emotes
                .orEmpty()
                .filterNotNull()
                .mapNotNull { emote ->
                    if (emote.tiers != null && emote.prefix != null) {
                        emote.tiers to emote.prefix
                    } else {
                        null
                    }
                }
                .flatMap { (tiers, prefix) ->
                    tiers.filterNotNull()
                        .mapNotNull { tier ->
                            val url = tier.images?.first()?.url
                            if (tier.bits != null && url != null) {
                                CheerEmote(
                                    name = prefix,
                                    minBits = tier.bits,
                                    color = tier.color,
                                    images = tier.images
                                        .filterNotNull()
                                        .map { image ->
                                            CheerEmote.Image(
                                                theme = image.theme,
                                                isAnimated = image.isAnimated,
                                                dpiScale = image.dpiScale.toFloat(),
                                                url = image.url
                                            )
                                        }
                                )
                            } else {
                                null
                            }
                        }
                }

        } catch (e: Exception) {
            helix.getCheerEmotes(
                helixClientId,
                helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) },
                userId
            ).emotes
        }
    }

    override suspend fun loadEmotesFromSet(
        helixClientId: String?,
        helixToken: String?,
        setIds: List<String>
    ): List<TwitchEmote>? = withContext(Dispatchers.IO) {
        helix.getEmotesFromSet(
            helixClientId,
            helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) },
            setIds
        ).emotes
    }

    override suspend fun loadUserFollowing(
        helixClientId: String?,
        helixToken: String?,
        userId: String?,
        channelId: String?,
        gqlClientId: String?,
        gqlToken: String?,
        userLogin: String?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!gqlToken.isNullOrBlank()) gql.loadFollowingUser(
                gqlClientId,
                gqlToken.let { TwitchApiHelper.addTokenPrefixGQL(it) },
                userLogin
            ).following else throw Exception()
        } catch (e: Exception) {
            helix.getUserFollows(
                helixClientId,
                helixToken?.let { TwitchApiHelper.addTokenPrefixHelix(it) },
                userId,
                channelId
            ).total == 1
        }
    }

    override suspend fun loadGameFollowing(
        gqlClientId: String?,
        gqlToken: String?,
        gameName: String?
    ): Boolean = withContext(Dispatchers.IO) {
        gql.loadFollowingGame(
            gqlClientId,
            gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) },
            gameName
        ).following
    }

    override suspend fun followUser(
        gqlClientId: String?,
        gqlToken: String?,
        userId: String?
    ): Boolean = withContext(Dispatchers.IO) {
        gql.loadFollowUser(
            gqlClientId,
            gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) },
            userId
        ).error.isNullOrBlank()
    }

    override suspend fun unfollowUser(
        gqlClientId: String?,
        gqlToken: String?,
        userId: String?
    ): Boolean = withContext(Dispatchers.IO) {
        !gql.loadUnfollowUser(
            gqlClientId,
            gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) },
            userId
        ).isJsonNull
    }

    override suspend fun followGame(
        gqlClientId: String?,
        gqlToken: String?,
        gameId: String?
    ): Boolean = withContext(Dispatchers.IO) {
        !gql.loadFollowGame(
            gqlClientId,
            gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) },
            gameId
        ).isJsonNull
    }

    override suspend fun unfollowGame(
        gqlClientId: String?,
        gqlToken: String?,
        gameId: String?
    ): Boolean = withContext(Dispatchers.IO) {
        !gql.loadUnfollowGame(
            gqlClientId,
            gqlToken?.let { TwitchApiHelper.addTokenPrefixGQL(it) },
            gameId
        ).isJsonNull
    }
}
