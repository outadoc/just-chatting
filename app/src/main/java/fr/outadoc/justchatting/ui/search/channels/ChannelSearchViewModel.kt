package fr.outadoc.justchatting.ui.search.channels

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import fr.outadoc.justchatting.model.helix.channel.ChannelSearch
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.ui.common.PagedListViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class ChannelSearchViewModel(
    private val repository: TwitchService
) : PagedListViewModel<ChannelSearch>() {

    private val query = MutableStateFlow<String?>(null)

    private val _result: Flow<Pager<*, ChannelSearch>> =
        query.filterNotNull()
            .distinctUntilChanged()
            .map { query ->
                repository.loadSearchChannels(query, viewModelScope)
            }

    override val result: Flow<Pager<*, ChannelSearch>> = _result

    fun setQuery(query: String) {
        this.query.value = query
    }
}
