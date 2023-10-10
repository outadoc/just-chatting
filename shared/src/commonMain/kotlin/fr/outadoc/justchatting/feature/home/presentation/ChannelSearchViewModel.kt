package fr.outadoc.justchatting.feature.home.presentation

import androidx.paging.PagingData
import androidx.paging.cachedIn
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSearch
import fr.outadoc.justchatting.component.chatapi.domain.repository.TwitchRepository
import fr.outadoc.justchatting.lifecycle.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.seconds

class ChannelSearchViewModel(
    private val repository: TwitchRepository,
) : ViewModel() {

    data class State(
        val query: String = "",
        val isActive: Boolean = false,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pagingData: Flow<PagingData<ChannelSearch>> =
        state.mapNotNull { state -> state.query }
            .distinctUntilChanged()
            .debounce(0.3.seconds)
            .flatMapLatest { query -> repository.loadSearchChannels(query) }
            .cachedIn(viewModelScope)

    fun onQueryChange(query: String) {
        _state.update { state ->
            state.copy(query = query)
        }
    }

    fun onActiveChange(isActive: Boolean) {
        _state.update { state ->
            state.copy(isActive = isActive)
        }
    }

    fun onDismiss() {
        _state.value = State()
    }

    fun onClear() {
        _state.update { state ->
            state.copy(query = "")
        }
    }
}
