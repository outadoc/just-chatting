package com.github.andreyasadchy.xtra.repository.datasourceGQL

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import kotlinx.coroutines.CoroutineScope

class ChannelClipsDataSourceGQL(
    private val clientId: String?,
    private val channelLogin: String?,
    private val sort: String?,
    private val api: GraphQLRepository,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Clip>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Clip>) {
        loadInitial(params, callback) {
            val get = api.loadChannelClips(clientId, channelLogin, sort, params.requestedLoadSize, offset)
            offset = get.cursor
            get.data
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Clip>) {
        loadRange(params, callback) {
            val get = api.loadChannelClips(clientId, channelLogin, sort, params.loadSize, offset)
            if (offset != null && offset != "") {
                offset = get.cursor
                get.data
            } else mutableListOf()
        }
    }

    class Factory(
        private val clientId: String?,
        private val channelLogin: String?,
        private val sort: String?,
        private val api: GraphQLRepository,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Clip, ChannelClipsDataSourceGQL>() {

        override fun create(): DataSource<Int, Clip> =
                ChannelClipsDataSourceGQL(clientId, channelLogin, sort, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
