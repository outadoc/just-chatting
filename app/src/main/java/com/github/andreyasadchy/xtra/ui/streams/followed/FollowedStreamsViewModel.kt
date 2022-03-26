package com.github.andreyasadchy.xtra.ui.streams.followed

import androidx.core.util.Pair
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
        repository.loadFollowedStreams(it.userId, it.helixClientId, it.helixToken, it.gqlClientId, it.gqlToken, it.apiPref, it.thumbnailsEnabled, viewModelScope)
    }

    fun loadStreams(userId: String? = null, helixClientId: String? = null, helixToken: String? = null, gqlClientId: String? = null, gqlToken: String? = null, apiPref: ArrayList<Pair<Long?, String?>?>, thumbnailsEnabled: Boolean) {
        Filter(userId, helixClientId, helixToken, gqlClientId, gqlToken, apiPref, thumbnailsEnabled).let {
            if (filter.value != it) {
                filter.value = it
            }
        }
    }

    private data class Filter(
        val userId: String?,
        val helixClientId: String?,
        val helixToken: String?,
        val gqlClientId: String?,
        val gqlToken: String?,
        val apiPref: ArrayList<Pair<Long?, String?>?>,
        val thumbnailsEnabled: Boolean)
}