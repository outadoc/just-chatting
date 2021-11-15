package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.game.Game
import kotlinx.coroutines.CoroutineScope

class GamesDataSource(
    private val clientId: String?,
    private val userToken: String?,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Game>(coroutineScope) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Game>) {
        loadInitial(params, callback) {
            api.getTopGames(clientId, userToken, params.requestedLoadSize, null).data
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Game>) {
        loadRange(params, callback) {
            api.getTopGames(clientId, userToken, params.loadSize, null).data
        }
    }

    class Factory(
        private val clientId: String?,
        private val userToken: String?,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Game, GamesDataSource>() {

        override fun create(): DataSource<Int, Game> = GamesDataSource(clientId, userToken, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
