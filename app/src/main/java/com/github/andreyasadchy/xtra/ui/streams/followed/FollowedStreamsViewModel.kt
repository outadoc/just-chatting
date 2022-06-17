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
    private val repository: TwitchService
) : PagedListViewModel<Stream>() {

    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Stream>> = Transformations.map(filter) {
        repository.loadFollowedStreams(
            userId = it.userId,
            helixClientId = it.helixClientId,
            helixToken = it.helixToken,
            coroutineScope = viewModelScope
        )
    }

    fun loadStreams(
        userId: String? = null,
        helixClientId: String? = null,
        helixToken: String? = null,
    ) {
        Filter(
            userId = userId,
            helixClientId = helixClientId,
            helixToken = helixToken
        ).let {
            if (filter.value != it) {
                filter.value = it
            }
        }
    }

    private data class Filter(
        val userId: String?,
        val helixClientId: String?,
        val helixToken: String?
    )
}
