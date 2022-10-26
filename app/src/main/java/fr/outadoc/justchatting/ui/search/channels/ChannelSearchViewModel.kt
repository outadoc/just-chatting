package fr.outadoc.justchatting.ui.search.channels

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.flatMap
import fr.outadoc.justchatting.model.helix.channel.ChannelSearch
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.ui.common.PagedListViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class ChannelSearchViewModel(
    private val repository: TwitchService
) : PagedListViewModel<ChannelSearch>() {

    private val query = MutableStateFlow<String?>(null)

    override val pagingData: Flow<PagingData<ChannelSearch>> =
        query.filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { query ->
                repository.loadSearchChannels(query)
                    .flow
                    .map { page ->
                        page.flatMap { searchResponse ->
                            searchResponse.data.orEmpty()
                                .let { search -> repository.mapSearchWithUserProfileImages(search) }
                        }
                    }
            }
            .cachedIn(viewModelScope)

    fun setQuery(query: String) {
        this.query.value = query
    }
}
