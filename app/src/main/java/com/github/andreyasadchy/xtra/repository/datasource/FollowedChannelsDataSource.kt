package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import kotlinx.coroutines.CoroutineScope

class FollowedChannelsDataSource(
    private val clientId: String?,
    private val userToken: String?,
    private val userId: String,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Follow>(coroutineScope) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Follow>) {
        loadInitial(params, callback) {
            api.getFollowedChannels(clientId, userToken, userId, params.requestedLoadSize, null).data
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Follow>) {
        loadRange(params, callback) {
            api.getFollowedChannels(clientId, userToken, userId, params.loadSize, null).data
        }
    }

    class Factory(
        private val clientId: String?,
        private val userToken: String?,
        private val userId: String,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Follow, FollowedChannelsDataSource>() {

        override fun create(): DataSource<Int, Follow> =
                FollowedChannelsDataSource(clientId, userToken, userId, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
