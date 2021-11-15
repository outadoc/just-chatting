package com.github.andreyasadchy.xtra.ui.follow.channels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class FollowedChannelsViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService) : PagedListViewModel<Follow>() {

    private val _sortText = MutableLiveData<CharSequence>()
    val sortText: LiveData<CharSequence>
        get() = _sortText
    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Follow>> = Transformations.map(filter) {
        repository.loadFollowedChannels(it.clientId, it.token, it.user.id, viewModelScope)
    }

    init {
        _sortText.value = context.getString(R.string.sort_and_order, context.getString(R.string.last_broadcast), context.getString(R.string.descending))
    }

    fun setUser(clientId: String?, token: String?, user: User) {
        if (filter.value == null) {
            filter.value = Filter(clientId, token, user)
        }
    }

    fun filter(text: CharSequence) {
        _sortText.value = text
    }

    private data class Filter(
            val clientId: String?,
            val token: String?,
            val user: User)
}
