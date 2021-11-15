package com.github.andreyasadchy.xtra.ui.streams.followed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class FollowedStreamsViewModel @Inject constructor(val repository: TwitchService) : PagedListViewModel<Stream>() {

    private val filter = MutableLiveData<Pair<User, Boolean>>()
    private var clientId = MutableLiveData<String>()
    override val result: LiveData<Listing<Stream>> = Transformations.map(filter) {
        repository.loadFollowedStreams(clientId.value, it.first.token, it.first.id, it.second, viewModelScope)
    }

    fun init(clientId: String?, user: User, thumbnailsEnabled: Boolean) {
        if (this.clientId.value != clientId) {
            this.clientId.value = clientId
        }
        val value = filter.value
        if (value == null || value.second != thumbnailsEnabled) {
            filter.value = user to thumbnailsEnabled
        }
    }
}