package com.github.andreyasadchy.xtra.ui.games

import androidx.core.util.Pair
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
        repository.loadTopGames(it.helixClientId, it.helixToken, it.gqlClientId, it.tags, it.apiPref, viewModelScope)
    }

    fun loadGames(helixClientId: String? = null, helixToken: String? = null, gqlClientId: String? = null, tags: List<String>? = null, apiPref: ArrayList<Pair<Long?, String?>?>) {
        Filter(helixClientId, helixToken, gqlClientId, tags, apiPref).let {
            if (filter.value != it) {
                filter.value = it
            }
        }
    }

    private data class Filter(
        val helixClientId: String?,
        val helixToken: String?,
        val gqlClientId: String?,
        val tags: List<String>?,
        val apiPref: ArrayList<Pair<Long?, String?>?>)
}
