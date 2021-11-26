package com.github.andreyasadchy.xtra.ui.streams.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class StreamsViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Stream>() {

    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Stream>> = Transformations.map(filter) {
        if (it.usehelix)
            repository.loadStreams(it.clientId, it.token, it.game, it.languages, viewModelScope)
        else {
            if (it.game == null)
                repository.loadTopStreamsGQL(it.clientId, viewModelScope)
            else
                repository.loadGameStreamsGQL(it.clientId, it.game, viewModelScope)
        }
    }

    fun loadStreams(usehelix: Boolean, clientId: String?, token: String? = "", channelId: String? = null,  game: String? = null, languages: String? = null) {
        Filter(usehelix, clientId, token, channelId, game, languages).let {
            if (filter.value != it) {
                filter.value = it
            }
        }
    }

    private data class Filter(
            val usehelix: Boolean,
            val clientId: String?,
            val token: String?,
            val channelId: String?,
            val game: String?,
            val languages: String?)
}
