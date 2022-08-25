package com.github.andreyasadchy.xtra.ui.streams.followed

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

class FollowedStreamsViewModel @Inject constructor(
    private val repository: TwitchService,
) : PagedListViewModel<Stream>() {

    private val _result = MutableStateFlow<Listing<Stream>?>(null)
    override val result: LiveData<Listing<Stream>> = _result.filterNotNull().asLiveData()

    fun loadStreams() = viewModelScope.launch {
        _result.value = repository.loadFollowedStreams(viewModelScope)
    }
}
