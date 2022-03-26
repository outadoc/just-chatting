package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.StreamsQuery
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.LocalFollowChannelRepository
import kotlinx.coroutines.CoroutineScope

class FollowedStreamsDataSource(
    private val localFollowsChannel: LocalFollowChannelRepository,
    private val userId: String?,
    private val helixClientId: String?,
    private val helixToken: String?,
    private val helixApi: HelixApi,
    private val gqlClientId: String?,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Stream>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        loadInitial(params, callback) {
            val list = mutableListOf<Stream>()
            val userIds = mutableListOf<String>()
            if (localFollowsChannel.loadFollows().isNotEmpty()) {
                val ids = mutableListOf<String>()
                for (i in localFollowsChannel.loadFollows()) {
                    ids.add(i.user_id)
                }
                for (localIds in ids.chunked(100)) {
                    val streams = mutableListOf<Stream>()
/*                    if (useHelix) {
                        helixApi.getStreams(helixClientId, helixToken, localIds).data?.let { streams.addAll(it) }*/
                    val get = apolloClient(XtraModule(), gqlClientId).query(StreamsQuery(Optional.Present(localIds))).execute().data?.users
                    if (get != null) {
                        for (i in get) {
                            streams.add(
                                Stream(id = i?.stream?.id, user_id = i?.id, user_login = i?.login, user_name = i?.displayName,
                                    game_id = i?.stream?.game?.id, game_name = i?.stream?.game?.displayName, type = i?.stream?.type,
                                    title = i?.stream?.title, viewer_count = i?.stream?.viewersCount, started_at = i?.stream?.createdAt,
                                    thumbnail_url = i?.stream?.previewImageURL, profileImageURL = i?.profileImageURL))
                        }
                    }
                    for (i in streams) {
                        if (i.viewer_count != null) {
/*                            if (useHelix) { i.user_id?.let { userIds.add(it) } }*/
                            list.add(i)
                        }
                    }
                }
            }
            if (!helixToken.isNullOrBlank()) {
                val get = helixApi.getFollowedStreams(helixClientId, helixToken, userId, 100, offset)
                if (get.data != null) {
                    for (i in get.data) {
                        val item = list.find { it.user_id == i.user_id }
                        if (item == null) {
                            i.user_id?.let { userIds.add(it) }
                            list.add(i)
                        }
                    }
                    offset = get.pagination?.cursor
                }
            }
            if (userIds.isNotEmpty()) {
                for (ids in userIds.chunked(100)) {
                    val users = helixApi.getUserById(helixClientId, helixToken, ids).data
                    if (users != null) {
                        for (i in users) {
                            val item = list.find { it.user_id == i.id }
                            if (item != null) {
                                item.profileImageURL = i.profile_image_url
                            }
                        }
                    }
                }
            }
            list.sortByDescending { it.viewer_count }
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Stream>) {
        loadRange(params, callback) {
            val list = mutableListOf<Stream>()
            if (offset != null && offset != "") {
                val userIds = mutableListOf<String>()
                if (!helixToken.isNullOrBlank()) {
                    val get = helixApi.getFollowedStreams(helixClientId, helixToken, userId, 100, offset)
                    if (get.data != null) {
                        for (i in get.data) {
                            val item = list.find { it.user_id == i.user_id }
                            if (item == null) {
                                i.user_id?.let { userIds.add(it) }
                                list.add(i)
                            }
                        }
                        offset = get.pagination?.cursor
                    }
                }
                if (userIds.isNotEmpty()) {
                    for (ids in userIds.chunked(100)) {
                        val users = helixApi.getUserById(helixClientId, helixToken, ids).data
                        if (users != null) {
                            for (i in users) {
                                val item = list.find { it.user_id == i.id }
                                if (item != null) {
                                    item.profileImageURL = i.profile_image_url
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
        private val localFollowsChannel: LocalFollowChannelRepository,
        private val userId: String?,
        private val helixClientId: String?,
        private val helixToken: String?,
        private val helixApi: HelixApi,
        private val gqlClientId: String?,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Stream, FollowedStreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
                FollowedStreamsDataSource(localFollowsChannel, userId, helixClientId, helixToken, helixApi, gqlClientId, coroutineScope).also(sourceLiveData::postValue)
    }
}