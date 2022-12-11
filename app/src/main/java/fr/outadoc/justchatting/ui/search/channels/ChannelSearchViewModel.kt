package fr.outadoc.justchatting.ui.search.channels

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.flatMap
import fr.outadoc.justchatting.component.twitch.model.helix.channel.ChannelSearch
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.ui.common.PagedListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.seconds

class ChannelSearchViewModel(
    private val repository: TwitchService
) : PagedListViewModel<ChannelSearch>() {

    data class State(val query: String = "")

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override val pagingData: Flow<PagingData<ChannelSearch>> =
        state.mapNotNull { state -> state.query }
            .distinctUntilChanged()
            .debounce(0.5.seconds)
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

    fun onQueryChange(query: String) {
        _state.update { state ->
            state.copy(query = query)
        }
    }
}
