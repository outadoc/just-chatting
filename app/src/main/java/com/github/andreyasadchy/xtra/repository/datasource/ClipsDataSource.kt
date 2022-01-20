package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import kotlinx.coroutines.CoroutineScope

class ClipsDataSource(
    private val clientId: String?,
    private val userToken: String?,
    private val channelId: String?,
    private val channelLogin: String?,
    private val gameId: String?,
    private val started_at: String?,
    private val ended_at: String?,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Clip>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Clip>) {
        loadInitial(params, callback) {
            val get = api.getClips(clientId, userToken, channelId, gameId, started_at, ended_at, params.requestedLoadSize, offset)
            val list = mutableListOf<Clip>()
            get.data?.let { list.addAll(it) }
            val userIds = mutableListOf<String>()
            val gameIds = mutableListOf<String>()
            for (i in list) {
                if (channelLogin != null) {
                    i.broadcaster_login = channelLogin
                } else {
                    i.broadcaster_id?.let { userIds.add(it) }
                }
                if (gameId == null) {
                    i.game_id?.let { gameIds.add(it) }
                }
            }
            if (userIds.isNotEmpty()) {
                val users = api.getUserById(clientId, userToken, userIds).data
                if (users != null) {
                    for (i in users) {
                        val items = list.filter { it.broadcaster_id == i.id }
                        for (item in items) {
                            item.broadcaster_login = i.login
                            item.profileImageURL = i.profile_image_url
                        }
                    }
                }
            }
            if (gameIds.isNotEmpty()) {
                val games = api.getGames(clientId, userToken, gameIds).data
                if (games != null) {
                    for (i in games) {
                        val items = list.filter { it.game_id == i.id }
                        for (item in items) {
                            item.game_name = i.name
                        }
                    }
                }
            }
            offset = get.pagination?.cursor
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Clip>) {
        loadRange(params, callback) {
            val get = api.getClips(clientId, userToken, channelId, gameId, started_at, ended_at, params.loadSize, offset)
            val list = mutableListOf<Clip>()
            if (offset != null && offset != "") {
                get.data?.let { list.addAll(it) }
                val userIds = mutableListOf<String>()
                val gameIds = mutableListOf<String>()
                for (i in list) {
                    if (channelLogin != null) {
                        i.broadcaster_login = channelLogin
                    } else {
                        i.broadcaster_id?.let { userIds.add(it) }
                    }
                    if (gameId == null) {
                        i.game_id?.let { gameIds.add(it) }
                    }
                }
                if (userIds.isNotEmpty()) {
                    val users = api.getUserById(clientId, userToken, userIds).data
                    if (users != null) {
                        for (i in users) {
                            val items = list.filter { it.broadcaster_id == i.id }
                            for (item in items) {
                                item.broadcaster_login = i.login
                                item.profileImageURL = i.profile_image_url
                            }
                        }
                    }
                }
                if (gameIds.isNotEmpty()) {
                    val games = api.getGames(clientId, userToken, gameIds).data
                    if (games != null) {
                        for (i in games) {
                            val items = list.filter { it.game_id == i.id }
                            for (item in items) {
                                item.game_name = i.name
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
        private val channelId: String?,
        private val channelLogin: String?,
        private val gameId: String?,
        private val started_at: String?,
        private val ended_at: String?,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Clip, ClipsDataSource>() {

        override fun create(): DataSource<Int, Clip> =
                ClipsDataSource(clientId, userToken, channelId, channelLogin, gameId, started_at, ended_at, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
