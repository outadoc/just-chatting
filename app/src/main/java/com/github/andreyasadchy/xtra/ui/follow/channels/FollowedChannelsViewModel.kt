package com.github.andreyasadchy.xtra.ui.follow.channels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class FollowedChannelsViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService) : PagedListViewModel<Follow>() {

    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Follow>> = Transformations.map(filter) {
        repository.loadFollowedChannels(it.clientId, it.user.token, it.user.id, viewModelScope)
    }

    fun setUser(clientId: String?, user: User) {
        if (filter.value == null) {
            filter.value = Filter(clientId, user)
        }
    }

    private data class Filter(
            val clientId: String?,
            val user: User)
}
