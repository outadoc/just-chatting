package com.github.andreyasadchy.xtra.ui.games

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class GamesViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Game>() {

    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Game>> = Transformations.map(filter) {
        repository.loadTopGames(it.clientId, it.token, viewModelScope)
    }

    fun loadGames(clientId: String?, token: String?) {
        Filter(clientId, token).let {
            if (filter.value != it) {
                filter.value = it
            }
        }
    }

    private data class Filter(
        val clientId: String?,
        val token: String?)
}
