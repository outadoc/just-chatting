package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.model.helix.video.Video
import kotlinx.coroutines.CoroutineScope

class VideosDataSource private constructor(
    private val clientId: String?,
    private val userToken: String?,
    private val game: String?,
    private val period: Period,
    private val broadcastTypes: BroadcastType,
    private val language: String?,
    private val sort: Sort,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Video>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Video>) {
        loadInitial(params, callback) {
            val get = api.getTopVideos(clientId, userToken, game, period, broadcastTypes, language, sort, params.requestedLoadSize, offset)
            val list = mutableListOf<Video>()
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

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Video>) {
        loadRange(params, callback) {
            val get = api.getTopVideos(clientId, userToken, game, period, broadcastTypes, language, sort, params.loadSize, offset)
            val list = mutableListOf<Video>()
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

    class Factory (
        private val clientId: String?,
        private val userToken: String?,
        private val game: String?,
        private val period: Period,
        private val broadcastTypes: BroadcastType,
        private val language: String?,
        private val sort: Sort,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Video, VideosDataSource>() {

        override fun create(): DataSource<Int, Video> =
                VideosDataSource(clientId, userToken, game, period, broadcastTypes, language, sort, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
