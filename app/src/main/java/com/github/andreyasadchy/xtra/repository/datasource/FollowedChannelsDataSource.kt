package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.UserLastBroadcastQuery
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.model.helix.follows.Order
import com.github.andreyasadchy.xtra.model.helix.follows.Sort
import com.github.andreyasadchy.xtra.repository.LocalFollowRepository
import kotlinx.coroutines.CoroutineScope

class FollowedChannelsDataSource(
    private val localFollows: LocalFollowRepository,
    private val gqlClientId: String?,
    private val helixClientId: String?,
    private val userToken: String?,
    private val userId: String,
    private val sort: Sort,
    private val order: Order,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Follow>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Follow>) {
        loadInitial(params, callback) {
            val list = mutableListOf<Follow>()
            for (i in localFollows.loadFollows()) {
                list.add(Follow(to_id = i.user_id, to_login = i.user_login, to_name = i.user_name, profileImageURL = i.channelLogo, followLocal = true))
            }
            if (userId != "") {
                val get = api.getFollowedChannels(helixClientId, userToken, userId, 100, offset)
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
                if (userId != "") {
                    val get = api.getFollowedChannels(helixClientId, userToken, userId, 100, offset)
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

    class Factory(
        private val localFollows: LocalFollowRepository,
        private val gqlClientId: String?,
        private val helixClientId: String?,
        private val userToken: String?,
        private val userId: String,
        private val sort: Sort,
        private val order: Order,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Follow, FollowedChannelsDataSource>() {

        override fun create(): DataSource<Int, Follow> =
                FollowedChannelsDataSource(localFollows, gqlClientId, helixClientId, userToken, userId, sort, order, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
