package fr.outadoc.justchatting.ui.streams.followed

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.ui.common.PagedListViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class FollowedStreamsViewModel(
    private val repository: TwitchService
) : PagedListViewModel<Stream>() {

    private val _result: MutableStateFlow<Pager<*, Stream>?> = MutableStateFlow(null)
    override val result: Flow<Pager<*, Stream>> = _result.filterNotNull()

    fun loadStreams() = viewModelScope.launch {
        _result.value = repository.loadFollowedStreams(viewModelScope)
    }
}
