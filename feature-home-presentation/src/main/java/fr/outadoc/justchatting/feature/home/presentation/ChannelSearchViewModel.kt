package fr.outadoc.justchatting.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.flatMap
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSearch
import fr.outadoc.justchatting.component.chatapi.domain.repository.TwitchRepository
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
    private val repository: TwitchRepository
) : ViewModel() {

    data class State(val query: String = "")

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pagingData: Flow<PagingData<ChannelSearch>> =
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
