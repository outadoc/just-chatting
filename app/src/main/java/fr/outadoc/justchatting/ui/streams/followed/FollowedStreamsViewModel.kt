package fr.outadoc.justchatting.ui.streams.followed

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.repository.Listing
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.ui.common.PagedListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class FollowedStreamsViewModel(
    private val repository: TwitchService
) : PagedListViewModel<Stream>() {

    private val _result = MutableStateFlow<Listing<Stream>?>(null)
    override val result: LiveData<Listing<Stream>> = _result.filterNotNull().asLiveData()

    fun loadStreams() = viewModelScope.launch {
        _result.value = repository.loadFollowedStreams(viewModelScope)
    }
}
