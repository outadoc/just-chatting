package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import kotlinx.coroutines.CoroutineScope

class FollowedStreamsDataSource(
    private val clientId: String?,
    private val userToken: String?,
    private val user_id: String,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Stream>(coroutineScope) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        loadInitial(params, callback) {
            api.getFollowedStreams(clientId, userToken, user_id, params.requestedLoadSize, null).data
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Stream>) {
        loadRange(params, callback) {
            api.getFollowedStreams(clientId, userToken, user_id, params.loadSize, null).data
        }
    }

    class Factory(
        private val clientId: String?,
        private val userToken: String?,
        private val user_id: String,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Stream, FollowedStreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
                FollowedStreamsDataSource(clientId, userToken, user_id, api, coroutineScope).also(sourceLiveData::postValue)
    }
}