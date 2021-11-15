package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import kotlinx.coroutines.CoroutineScope

class ClipsDataSource(
    private val clientId: String?,
    private val userToken: String?,
    private val channelName: String?,
    private val gameName: String?,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Clip>(coroutineScope) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Clip>) {
        loadInitial(params, callback) {
            api.getClips(clientId, userToken, channelName, gameName, params.requestedLoadSize, null).data
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Clip>) {
        loadRange(params, callback) {
            api.getClips(clientId, userToken, channelName, gameName, params.loadSize, null).data
        }
    }

    class Factory(
        private val clientId: String?,
        private val userToken: String?,
        private val channelName: String?,
        private val gameName: String?,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Clip, ClipsDataSource>() {

        override fun create(): DataSource<Int, Clip> =
                ClipsDataSource(clientId, userToken, channelName, gameName, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
