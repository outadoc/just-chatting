package com.github.andreyasadchy.xtra.repository.datasource

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.util.Pair
import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.andreyasadchy.xtra.FollowedUsersQuery
import com.github.andreyasadchy.xtra.UserLastBroadcastQuery
import com.github.andreyasadchy.xtra.XtraApp
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientWithTokenFactory.apolloClientWithToken
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.model.helix.follows.Order
import com.github.andreyasadchy.xtra.model.helix.follows.Sort
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.LocalFollowChannelRepository
import com.github.andreyasadchy.xtra.repository.OfflineRepository
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.DownloadUtils
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class FollowedChannelsDataSource(
    private val localFollowsChannel: LocalFollowChannelRepository,
    private val offlineRepository: OfflineRepository,
    private val userId: String?,
    private val helixClientId: String?,
    private val helixToken: String?,
    private val helixApi: HelixApi,
    private val gqlClientId: String?,
    private val gqlToken: String?,
    private val gqlApi: GraphQLRepository,
    private val apiPref: ArrayList<Pair<Long?, String?>?>,
    private val sort: Sort,
    private val order: Order,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Follow>(coroutineScope) {
    private var api: String? = null
    private var offset: String? = null
    private var nextPage: Boolean = true

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Follow>) {
        loadInitial(params, callback) {
            val list = mutableListOf<Follow>()
            for (i in localFollowsChannel.loadFollows()) {
                list.add(Follow(to_id = i.user_id, to_login = i.user_login, to_name = i.user_name, profileImageURL = i.channelLogo, followLocal = true))
            }
            val remote = try {
                when (apiPref.elementAt(0)?.second) {
                    C.HELIX -> if (!helixToken.isNullOrBlank()) helixInitial(params) else throw Exception()
                    C.GQL_QUERY -> if (!gqlToken.isNullOrBlank()) gqlQueryInitial(params) else throw Exception()
                    C.GQL -> if (!gqlToken.isNullOrBlank()) gqlInitial(params) else throw Exception()
                    else -> mutableListOf()
                }
            } catch (e: Exception) {
                try {
                    when (apiPref.elementAt(1)?.second) {
                        C.HELIX -> if (!helixToken.isNullOrBlank()) helixInitial(params) else throw Exception()
                        C.GQL_QUERY -> if (!gqlToken.isNullOrBlank()) gqlQueryInitial(params) else throw Exception()
                        C.GQL -> if (!gqlToken.isNullOrBlank()) gqlInitial(params) else throw Exception()
                        else -> mutableListOf()
                    }
                } catch (e: Exception) {
                    try {
                        when (apiPref.elementAt(2)?.second) {
                            C.HELIX -> if (!helixToken.isNullOrBlank()) helixInitial(params) else throw Exception()
                            C.GQL_QUERY -> if (!gqlToken.isNullOrBlank()) gqlQueryInitial(params) else throw Exception()
                            C.GQL -> if (!gqlToken.isNullOrBlank()) gqlInitial(params) else throw Exception()
                            else -> mutableListOf()
                        }
                    } catch (e: Exception) {
                        mutableListOf()
                    }
                }
            }
            if (!remote.isNullOrEmpty()) {
                for (i in remote) {
                    val item = list.find { it.to_id == i.to_id }
                    if (item == null) {
                        i.followTwitch = true
                        list.add(i)
                    } else {
                        item.followTwitch = true
                    }
                }
            }
            val allIds = mutableListOf<String>()
            for (i in list) {
                if (i.profileImageURL == null || i.profileImageURL?.contains("image_manager_disk_cache") == true || i.lastBroadcast == null) {
                    i.to_id?.let { allIds.add(it) }
                }
            }
            if (allIds.isNotEmpty()) {
                for (ids in allIds.chunked(100)) {
                    val get = apolloClient(XtraModule(), gqlClientId).query(UserLastBroadcastQuery(Optional.Present(ids))).execute().data?.users
                    if (get != null) {
                        for (user in get) {
                            val item = list.find { it.to_id == user?.id }
                            if (item != null) {
                                if (item.followLocal) {
                                    if (item.profileImageURL == null || item.profileImageURL?.contains("image_manager_disk_cache") == true) {
                                        val appContext = XtraApp.INSTANCE.applicationContext
                                        item.to_id?.let { id -> user?.profileImageURL?.let { profileImageURL -> updateLocalUser(appContext, id, profileImageURL) } }
                                    }
                                } else {
                                    if (item.profileImageURL == null) {
                                        item.profileImageURL = user?.profileImageURL
                                    }
                                }
                                item.lastBroadcast = user?.lastBroadcast?.startedAt?.toString()
                            }
                        }
                    }
                }
            }
            if (order == Order.ASC) {
                when (sort) {
                    Sort.FOLLOWED_AT -> list.sortBy { it.followed_at }
                    Sort.LAST_BROADCAST -> list.sortBy { it.lastBroadcast }
                    else -> list.sortBy { it.to_login }
                }
            } else {
                when (sort) {
                    Sort.FOLLOWED_AT -> list.sortByDescending { it.followed_at }
                    Sort.LAST_BROADCAST -> list.sortByDescending { it.lastBroadcast }
                    else -> list.sortByDescending { it.to_login }
                }
            }
            list
        }
    }

    private suspend fun helixInitial(params: LoadInitialParams): List<Follow> {
        api = C.HELIX
        val get = helixApi.getFollowedChannels(helixClientId, helixToken, userId, 100, offset)
        return if (get.data != null) {
            offset = get.pagination?.cursor
            get.data
        } else mutableListOf()
    }

    private suspend fun gqlQueryInitial(params: LoadInitialParams): List<Follow> {
        api = C.GQL_QUERY
        val get1 = apolloClientWithToken(XtraModule(), gqlClientId, gqlToken)
            .query(FollowedUsersQuery(id = Optional.Present(userId), first = Optional.Present(100), after = Optional.Present(offset))).execute().data?.user?.follows
        val get = get1?.edges
        val list = mutableListOf<Follow>()
        if (get != null) {
            for (i in get) {
                list.add(
                    Follow(
                        to_id = i?.node?.id,
                        to_login = i?.node?.login,
                        to_name = i?.node?.displayName,
                        followed_at = i?.followedAt.toString(),
                        lastBroadcast = i?.node?.lastBroadcast?.startedAt.toString(),
                        profileImageURL = i?.node?.profileImageURL,
                    )
                )
            }
            offset = get.lastOrNull()?.cursor.toString()
            nextPage = get1.pageInfo?.hasNextPage ?: true
        }
        return list
    }

    private suspend fun gqlInitial(params: LoadInitialParams): List<Follow> {
        api = C.GQL
        val get = gqlApi.loadFollowedChannels(gqlClientId, gqlToken, 100, offset)
        return if (!get.data.isNullOrEmpty()) {
            offset = get.cursor
            get.data
        } else mutableListOf()
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Follow>) {
        loadRange(params, callback) {
            val list = if (!offset.isNullOrBlank()) {
                when (api) {
                    C.HELIX -> helixRange(params)
                    C.GQL_QUERY -> gqlQueryRange(params)
                    C.GQL -> gqlRange(params)
                    else -> mutableListOf()
                }
            } else mutableListOf()
            for (i in list) {
                val allIds = mutableListOf<String>()
                if (i.profileImageURL == null || i.lastBroadcast == null) {
                    i.to_id?.let { allIds.add(it) }
                }
                if (allIds.isNotEmpty()) {
                    for (ids in allIds.chunked(100)) {
                        val get = apolloClient(XtraModule(), gqlClientId).query(UserLastBroadcastQuery(Optional.Present(ids))).execute().data?.users
                        if (get != null) {
                            for (user in get) {
                                val item = list.find { it.to_id == user?.id }
                                if (item != null) {
                                    if (item.followLocal) {
                                        if (item.profileImageURL == null || item.profileImageURL?.contains("image_manager_disk_cache") == true) {
                                            val appContext = XtraApp.INSTANCE.applicationContext
                                            item.to_id?.let { id -> user?.profileImageURL?.let { profileImageURL -> updateLocalUser(appContext, id, profileImageURL) } }
                                        }
                                    } else {
                                        if (item.profileImageURL == null) {
                                            item.profileImageURL = user?.profileImageURL
                                        }
                                    }
                                    item.lastBroadcast = user?.lastBroadcast?.startedAt?.toString()
                                }
                            }
                        }
                    }
                }
            }
            list
        }
    }

    private suspend fun helixRange(params: LoadRangeParams): List<Follow> {
        val get = helixApi.getFollowedChannels(helixClientId, helixToken, userId, 100, offset)
        return if (get.data != null) {
            offset = get.pagination?.cursor
            get.data
        } else mutableListOf()
    }

    private suspend fun gqlQueryRange(params: LoadRangeParams): List<Follow> {
        val get1 = apolloClientWithToken(XtraModule(), gqlClientId, gqlToken)
            .query(FollowedUsersQuery(id = Optional.Present(userId), first = Optional.Present(100), after = Optional.Present(offset))).execute().data?.user?.follows
        val get = get1?.edges
        val list = mutableListOf<Follow>()
        if (get != null && nextPage && offset != null && offset != "") {
            for (i in get) {
                list.add(
                    Follow(
                        to_id = i?.node?.id,
                        to_login = i?.node?.login,
                        to_name = i?.node?.displayName,
                        followed_at = i?.followedAt.toString(),
                        lastBroadcast = i?.node?.lastBroadcast?.startedAt.toString(),
                        profileImageURL = i?.node?.profileImageURL,
                    )
                )
            }
            offset = get.lastOrNull()?.cursor.toString()
            nextPage = get1.pageInfo?.hasNextPage ?: true
        }
        return list
    }

    private suspend fun gqlRange(params: LoadRangeParams): List<Follow> {
        val get = gqlApi.loadFollowedChannels(gqlClientId, gqlToken, 100, offset)
        return if (!get.data.isNullOrEmpty()) {
            offset = get.cursor
            get.data
        } else mutableListOf()
    }

    private fun updateLocalUser(context: Context, userId: String, profileImageURL: String) {
        GlobalScope.launch {
            try {
                try {
                    Glide.with(context)
                        .asBitmap()
                        .load(TwitchApiHelper.getTemplateUrl(profileImageURL, "profileimage"))
                        .into(object: CustomTarget<Bitmap>() {
                            override fun onLoadCleared(placeholder: Drawable?) {

                            }

                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                DownloadUtils.savePng(context, "profile_pics", userId, resource)
                            }
                        })
                } catch (e: Exception) {

                }
                val downloadedLogo = File(context.filesDir.toString() + File.separator + "profile_pics" + File.separator + "${userId}.png").absolutePath
                localFollowsChannel.getFollowById(userId)?.let { localFollowsChannel.updateFollow(it.apply {
                    channelLogo = downloadedLogo }) }
                for (i in offlineRepository.getVideosByUserId(userId.toInt())) {
                    offlineRepository.updateVideo(i.apply {
                        channelLogo = downloadedLogo })
                }
            } catch (e: Exception) {

            }
        }
    }

    class Factory(
        private val localFollowsChannel: LocalFollowChannelRepository,
        private val offlineRepository: OfflineRepository,
        private val userId: String?,
        private val helixClientId: String?,
        private val helixToken: String?,
        private val helixApi: HelixApi,
        private val gqlClientId: String?,
        private val gqlToken: String?,
        private val gqlApi: GraphQLRepository,
        private val apiPref: ArrayList<Pair<Long?, String?>?>,
        private val sort: Sort,
        private val order: Order,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Follow, FollowedChannelsDataSource>() {

        override fun create(): DataSource<Int, Follow> =
                FollowedChannelsDataSource(localFollowsChannel, offlineRepository, userId, helixClientId, helixToken, helixApi, gqlClientId, gqlToken, gqlApi, apiPref, sort, order, coroutineScope).also(sourceLiveData::postValue)
    }
}
