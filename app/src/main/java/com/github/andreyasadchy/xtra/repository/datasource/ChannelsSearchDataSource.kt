package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.channel.Channel
import kotlinx.coroutines.CoroutineScope

class ChannelsSearchDataSource private constructor(
    private val clientId: String?,
    private val userToken: String?,
    private val query: String,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Channel>(coroutineScope) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Channel>) {
        loadInitial(params, callback) {
            api.getChannels(clientId, userToken, query, params.requestedLoadSize, null).data
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Channel>) {
        loadRange(params, callback) {
            api.getChannels(clientId, userToken, query, params.loadSize, null).data
        }
    }

    class Factory(
        private val clientId: String?,
        private val userToken: String?,
        private val query: String,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Channel, ChannelsSearchDataSource>() {

        override fun create(): DataSource<Int, Channel> =
                ChannelsSearchDataSource(clientId, userToken, query, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
