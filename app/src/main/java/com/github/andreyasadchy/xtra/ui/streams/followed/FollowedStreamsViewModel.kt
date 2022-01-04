package com.github.andreyasadchy.xtra.ui.streams.followed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class FollowedStreamsViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Stream>() {

    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Stream>> = Transformations.map(filter) {
        repository.loadFollowedStreams(it.usehelix, it.clientId, it.token, it.channelId, it.thumbnailsEnabled, viewModelScope)
    }

    fun loadStreams(usehelix: Boolean, clientId: String?, token: String? = "", channelId: String, thumbnailsEnabled: Boolean) {
        Filter(usehelix, clientId, token, channelId, thumbnailsEnabled).let {
            if (filter.value != it) {
                filter.value = it
            }
        }
    }

    private data class Filter(
        val usehelix: Boolean,
        val clientId: String?,
        val token: String?,
        val channelId: String,
        val thumbnailsEnabled: Boolean,)
}