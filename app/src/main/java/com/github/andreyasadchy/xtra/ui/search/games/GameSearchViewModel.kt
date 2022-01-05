package com.github.andreyasadchy.xtra.ui.search.games

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class GameSearchViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Game>() {

    private var useHelix = false
    private val query = MutableLiveData<String>()
    private var clientId = MutableLiveData<String>()
    private var token = MutableLiveData<String>()
    override val result: LiveData<Listing<Game>> = Transformations.map(query) {
        if (useHelix)
            repository.loadSearchGames(clientId.value, token.value, it, viewModelScope)
        else
            repository.loadSearchGamesGQL(clientId.value, it, viewModelScope)
    }

    fun setQuery(usehelix: Boolean, clientId: String?, query: String, token: String? = "") {
        if (useHelix != usehelix) {
            useHelix = usehelix
        }
        if (this.clientId.value != clientId) {
            this.clientId.value = clientId
        }
        if (this.token.value != token) {
            this.token.value = token
        }
        if (this.query.value != query) {
            this.query.value = query
        }
    }
}