package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.LocalFollowRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import kotlinx.coroutines.CoroutineScope

class FollowedStreamsDataSource(
    private val useHelix: Boolean,
    private val localFollows: LocalFollowRepository,
    private val repository: TwitchService,
    private val clientId: String?,
    private val userToken: String?,
    private val userId: String,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Stream>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        loadInitial(params, callback) {
            val list = mutableListOf<Stream>()
            val userIds = mutableListOf<String>()
            for (i in localFollows.loadFollows()) {
                val get = if (useHelix) {
                    val ids = mutableListOf<String>()
                    ids.add(i.user_id)
                    api.getStreams(clientId, userToken, ids).data?.firstOrNull()
                } else {
                    repository.loadStreamGQL(clientId, i.user_id)
                }
                if (get?.viewer_count != null) {
                    if (useHelix) { i.user_id.let { userIds.add(it) } }
                    list.add(get)
                }
            }
            if (useHelix && userId != "") {
                val get = api.getFollowedStreams(clientId, userToken, userId, params.requestedLoadSize, offset)
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
                val users = api.getUserById(clientId, userToken, userIds).data
                if (users != null) {
                    for (i in users) {
                        val item = list.find { it.user_id == i.id }
                        if (item != null) {
                            item.profileImageURL = i.profile_image_url
                        }
                    }
                }
            }
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Stream>) {
        loadRange(params, callback) {
            val list = mutableListOf<Stream>()
            if (offset != null && offset != "") {
                val userIds = mutableListOf<String>()
                if (useHelix && userId != "") {
                    val get = api.getFollowedStreams(clientId, userToken, userId, params.loadSize, offset)
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
                    val users = api.getUserById(clientId, userToken, userIds).data
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
            list
        }
    }

    class Factory(
        private val useHelix: Boolean,
        private val localFollows: LocalFollowRepository,
        private val repository: TwitchService,
        private val clientId: String?,
        private val userToken: String?,
        private val user_id: String,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Stream, FollowedStreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
                FollowedStreamsDataSource(useHelix, localFollows, repository, clientId, userToken, user_id, api, coroutineScope).also(sourceLiveData::postValue)
    }
}