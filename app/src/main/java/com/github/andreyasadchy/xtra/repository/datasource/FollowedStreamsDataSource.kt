package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.LocalFollowRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.coroutines.CoroutineScope

class FollowedStreamsDataSource(
    private val usehelix: Boolean,
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
            for (i in localFollows.loadFollows()) {
                val get = if (usehelix) {
                    repository.loadStream(clientId, userToken, i.user_id)
                } else {
                    repository.loadStreamGQL(clientId, i.user_id)
                }
                if (get.viewer_count != null) {
                    if (usehelix) { get.profileImageURL = i.user_id.let { api.getUserById(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, i.user_id).data?.first()?.profile_image_url } }
                    list.add(get)
                }
            }
            if (usehelix && userId != "") {
                val get = api.getFollowedStreams(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, userId, params.requestedLoadSize, offset)
                for (i in get.data) {
                    val item = list.find { it.user_id == i.user_id }
                    if (item == null) {
                        i.profileImageURL = i.user_id?.let { api.getUserById(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, i.user_id).data?.first()?.profile_image_url }
                        list.add(i)
                    }
                }
                offset = get.pagination?.cursor
            }
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Stream>) {
        loadRange(params, callback) {
            val list = mutableListOf<Stream>()
            if (usehelix && userId != "" && offset != null && offset != "") {
                val get = api.getFollowedStreams(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, userId, params.loadSize, offset)
                for (i in get.data) {
                    val item = list.find { it.user_id == i.user_id }
                    if (item == null) {
                        i.profileImageURL = i.user_id?.let { api.getUserById(clientId, userToken?.let { TwitchApiHelper.addTokenPrefix(it) }, i.user_id).data?.first()?.profile_image_url }
                        list.add(i)
                    }
                }
                offset = get.pagination?.cursor
            }
            list
        }
    }

    class Factory(
        private val usehelix: Boolean,
        private val localFollows: LocalFollowRepository,
        private val repository: TwitchService,
        private val clientId: String?,
        private val userToken: String?,
        private val user_id: String,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Stream, FollowedStreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
                FollowedStreamsDataSource(usehelix, localFollows, repository, clientId, userToken, user_id, api, coroutineScope).also(sourceLiveData::postValue)
    }
}