package com.github.andreyasadchy.xtra.repository.datasourceGQL

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import kotlinx.coroutines.CoroutineScope

class GameStreamsDataSourceGQL private constructor(
    private val clientId: String?,
    private val gameName: String?,
    private val tags: List<String>?,
    private val api: GraphQLRepository,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Stream>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        loadInitial(params, callback) {
            val get = api.loadGameStreams(clientId, gameName, tags, params.requestedLoadSize, offset)
            offset = get.cursor
            get.data
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Stream>) {
        loadRange(params, callback) {
            val get = api.loadGameStreams(clientId, gameName, tags, params.loadSize, offset)
            if (offset != null && offset != "") {
                offset = get.cursor
                get.data
            } else mutableListOf()
        }
    }

    class Factory(
        private val clientId: String?,
        private val gameName: String?,
        private val tags: List<String>?,
        private val api: GraphQLRepository,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Stream, GameStreamsDataSourceGQL>() {

        override fun create(): DataSource<Int, Stream> =
                GameStreamsDataSourceGQL(clientId, gameName, tags, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
