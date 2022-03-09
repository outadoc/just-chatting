package com.github.andreyasadchy.xtra.ui.follow.games

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class FollowedGamesViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService) : PagedListViewModel<Game>() {

    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Game>> = Transformations.map(filter) {
        repository.loadFollowedGames(it.gqlClientId, it.helixClientId, it.user.token, it.user.id, viewModelScope)
    }

    fun setUser(gqlClientId: String?, helixClientId: String?, user: User) {
        if (filter.value == null) {
            filter.value = Filter(gqlClientId, helixClientId, user)
        }
    }

    private data class Filter(
        val gqlClientId: String?,
        val helixClientId: String?,
        val user: User)
}
