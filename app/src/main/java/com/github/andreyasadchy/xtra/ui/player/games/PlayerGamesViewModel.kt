package com.github.andreyasadchy.xtra.ui.player.games

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.PagedList
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.datasource.BaseDataSourceFactory
import com.github.andreyasadchy.xtra.repository.datasource.BasePositionalDataSource
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class PlayerGamesViewModel @Inject constructor() : PagedListViewModel<Game>() {

    private val gamesList = MutableLiveData<List<Game>>()
    override val result: LiveData<Listing<Game>> = Transformations.map(gamesList) {
        val factory = GamesListDataSource.Factory(gamesList.value, viewModelScope)
        val config = PagedList.Config.Builder()
            .setPageSize(30)
            .setInitialLoadSizeHint(30)
            .setPrefetchDistance(10)
            .setEnablePlaceholders(false)
            .build()
        Listing.create(factory, config)
    }

    class GamesListDataSource(
        private val gamesList: List<Game>?,
        coroutineScope: CoroutineScope) : BasePositionalDataSource<Game>(coroutineScope) {

        override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Game>) {
            loadInitial(params, callback) {
                gamesList ?: mutableListOf()
            }
        }

        override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Game>) {
            loadRange(params, callback) {
                mutableListOf()
            }
        }

        class Factory(
            private val gamesList: List<Game>?,
            private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Game, GamesListDataSource>() {

            override fun create(): DataSource<Int, Game> =
                GamesListDataSource(gamesList, coroutineScope).also(sourceLiveData::postValue)
        }
    }

    fun loadGames(gamesList: List<Game>?) {
        if (this.gamesList.value != gamesList) {
            this.gamesList.value = gamesList
        }
    }
}