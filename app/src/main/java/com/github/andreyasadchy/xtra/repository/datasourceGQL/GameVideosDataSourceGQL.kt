package com.github.andreyasadchy.xtra.repository.datasourceGQL

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import kotlinx.coroutines.CoroutineScope

class GameVideosDataSourceGQL private constructor(
    private val clientId: String?,
    private val gameId: String?,
    private val gameName: String?,
    private val type: String?,
    private val sort: String?,
    private val api: GraphQLRepository,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Video>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Video>) {
        loadInitial(params, callback) {
            val get = api.loadGameVideos(clientId, gameName, type, sort, params.requestedLoadSize, offset)
            offset = get.cursor
            get.data
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Video>) {
        loadRange(params, callback) {
            val get = api.loadGameVideos(clientId, gameName, type, sort, params.loadSize, offset)
            if (offset != null && offset != "") {
                offset = get.cursor
                get.data
            } else mutableListOf()
        }
    }

    class Factory (
        private val clientId: String?,
        private val gameId: String?,
        private val gameName: String?,
        private val type: String?,
        private val sort: String?,
        private val api: GraphQLRepository,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Video, GameVideosDataSourceGQL>() {

        override fun create(): DataSource<Int, Video> =
                GameVideosDataSourceGQL(clientId, gameId, gameName, type, sort, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
