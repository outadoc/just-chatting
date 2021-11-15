package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import kotlinx.coroutines.CoroutineScope

class StreamsDataSource private constructor(
    private val clientId: String?,
    private val userToken: String?,
    private val game: String?,
    private val languages: String?,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Stream>(coroutineScope) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        loadInitial(params, callback) {
            api.getStreams(clientId, userToken, game, languages, params.requestedLoadSize, null).data
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Stream>) {
        loadRange(params, callback) {
            api.getStreams(clientId, userToken, game, languages, params.loadSize, null).data
        }
    }

    class Factory(
        private val clientId: String?,
        private val userToken: String?,
        private val game: String?,
        private val languages: String?,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Stream, StreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
                StreamsDataSource(clientId, userToken, game, languages, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
