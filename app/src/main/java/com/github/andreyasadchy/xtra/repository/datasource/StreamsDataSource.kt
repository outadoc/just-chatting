package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import kotlinx.coroutines.CoroutineScope

class StreamsDataSource private constructor(
    private val clientId: String?,
    private val userToken: String?,
    private val gameId: String?,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Stream>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Stream>) {
        loadInitial(params, callback) {
            val get = api.getTopStreams(clientId, userToken, gameId, null, params.requestedLoadSize, offset)
            val list = mutableListOf<Stream>()
            get.data?.let { list.addAll(it) }
            val ids = mutableListOf<String>()
            for (i in list) {
                i.user_id?.let { ids.add(it) }
            }
            if (ids.isNotEmpty()) {
                val users = api.getUserById(clientId, userToken, ids).data
                if (users != null) {
                    for (i in users) {
                        val items = list.filter { it.user_id == i.id }
                        for (item in items) {
                            item.profileImageURL = i.profile_image_url
                        }
                    }
                }
            }
            offset = get.pagination?.cursor
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Stream>) {
        loadRange(params, callback) {
            val get = api.getTopStreams(clientId, userToken, gameId, null, params.loadSize, offset)
            val list = mutableListOf<Stream>()
            if (offset != null && offset != "") {
                get.data?.let { list.addAll(it) }
                val ids = mutableListOf<String>()
                for (i in list) {
                    i.user_id?.let { ids.add(it) }
                }
                if (ids.isNotEmpty()) {
                    val users = api.getUserById(clientId, userToken, ids).data
                    if (users != null) {
                        for (i in users) {
                            val items = list.filter { it.user_id == i.id }
                            for (item in items) {
                                item.profileImageURL = i.profile_image_url
                            }
                        }
                    }
                }
                offset = get.pagination?.cursor
            }
            list
        }
    }

    class Factory(
        private val clientId: String?,
        private val userToken: String?,
        private val gameId: String?,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Stream, StreamsDataSource>() {

        override fun create(): DataSource<Int, Stream> =
                StreamsDataSource(clientId, userToken, gameId, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
