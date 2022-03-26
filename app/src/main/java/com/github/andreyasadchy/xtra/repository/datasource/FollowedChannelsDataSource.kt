package com.github.andreyasadchy.xtra.repository.datasource

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.andreyasadchy.xtra.UserLastBroadcastQuery
import com.github.andreyasadchy.xtra.XtraApp
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.model.helix.follows.Order
import com.github.andreyasadchy.xtra.model.helix.follows.Sort
import com.github.andreyasadchy.xtra.repository.LocalFollowChannelRepository
import com.github.andreyasadchy.xtra.repository.OfflineRepository
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
    private val sort: Sort,
    private val order: Order,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Follow>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Follow>) {
        loadInitial(params, callback) {
            val list = mutableListOf<Follow>()
            for (i in localFollowsChannel.loadFollows()) {
                list.add(Follow(to_id = i.user_id, to_login = i.user_login, to_name = i.user_name, profileImageURL = i.channelLogo, followLocal = true))
            }
            if (!helixToken.isNullOrBlank()) {
                val get = helixApi.getFollowedChannels(helixClientId, helixToken, userId, 100, offset)
                if (get.data != null) {
                    for (i in get.data) {
                        val item = list.find { it.to_id == i.to_id }
                        if (item == null) {
                            i.followTwitch = true
                            list.add(i)
                        } else {
                            item.followTwitch = true
                        }
                    }
                    offset = get.pagination?.cursor
                }
            }
            if (list.isNotEmpty()) {
                val allIds = list.mapNotNull { it.to_id }
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

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Follow>) {
        loadRange(params, callback) {
            val list = mutableListOf<Follow>()
            if (offset != null && offset != "") {
                if (!helixToken.isNullOrBlank()) {
                    val get = helixApi.getFollowedChannels(helixClientId, helixToken, userId, 100, offset)
                    if (get.data != null) {
                        for (i in get.data) {
                            val item = list.find { it.to_id == i.to_id }
                            if (item == null) {
                                i.followTwitch = true
                                list.add(i)
                            } else {
                                item.followTwitch = true
                            }
                        }
                        offset = get.pagination?.cursor
                    }
                }
                if (list.isNotEmpty()) {
                    val allIds = list.mapNotNull { it.to_id }
                    if (allIds.isNotEmpty()) {
                        for (ids in allIds.chunked(100)) {
                            val get = apolloClient(XtraModule(), gqlClientId).query(UserLastBroadcastQuery(Optional.Present(ids))).execute().data?.users
                            if (get != null) {
                                for (user in get) {
                                    val item = list.find { it.to_id == user?.id }
                                    if (item != null) {
                                        if (item.profileImageURL == null) {
                                            item.profileImageURL = user?.profileImageURL
                                        }
                                        item.lastBroadcast = user?.lastBroadcast?.startedAt?.toString()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            list
        }
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
        private val sort: Sort,
        private val order: Order,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Follow, FollowedChannelsDataSource>() {

        override fun create(): DataSource<Int, Follow> =
                FollowedChannelsDataSource(localFollowsChannel, offlineRepository, userId, helixClientId, helixToken, helixApi, gqlClientId, sort, order, coroutineScope).also(sourceLiveData::postValue)
    }
}
