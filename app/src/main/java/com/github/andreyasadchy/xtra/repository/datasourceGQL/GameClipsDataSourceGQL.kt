package com.github.andreyasadchy.xtra.repository.datasourceGQL

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import kotlinx.coroutines.CoroutineScope

class GameClipsDataSourceGQL(
    private val clientId: String?,
    private val gameId: String?,
    private val gameName: String?,
    private val sort: String?,
    private val api: GraphQLRepository,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Clip>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Clip>) {
        loadInitial(params, callback) {
            val get = api.loadGameClips(clientId, gameName, sort, params.requestedLoadSize, offset)
            offset = get.cursor
            get.data
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Clip>) {
        loadRange(params, callback) {
            val get = api.loadGameClips(clientId, gameName, sort, params.loadSize, offset)
            if (offset != null && offset != "") {
                offset = get.cursor
                get.data
            } else mutableListOf()
        }
    }

    class Factory(
        private val clientId: String?,
        private val gameId: String?,
        private val gameName: String?,
        private val sort: String?,
        private val api: GraphQLRepository,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Clip, GameClipsDataSourceGQL>() {

        override fun create(): DataSource<Int, Clip> =
                GameClipsDataSourceGQL(clientId, gameId, gameName, sort, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
