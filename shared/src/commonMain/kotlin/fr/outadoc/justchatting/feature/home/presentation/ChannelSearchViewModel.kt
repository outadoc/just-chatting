package fr.outadoc.justchatting.feature.home.presentation

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import fr.outadoc.justchatting.feature.home.domain.TwitchRepository
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSearchResult
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

internal class ChannelSearchViewModel(
    private val twitchRepository: TwitchRepository,
) : ViewModel() {

    data class State(
        val query: String = "",
        val isActive: Boolean = false,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pagingData: Flow<PagingData<ChannelSearchResult>> =
        state.mapNotNull { state -> state.query }
            .distinctUntilChanged()
            .debounce(0.3.seconds)
            .flatMapLatest { query ->
                if (query.isEmpty()) {
                    loadRecentChannels()
                } else {
                    loadSearchResults(query)
                }
            }
            .cachedIn(viewModelScope)

    private suspend fun loadSearchResults(query: String): Flow<PagingData<ChannelSearchResult>> {
        return twitchRepository.searchChannels(query)
    }

    private suspend fun loadRecentChannels(): Flow<PagingData<ChannelSearchResult>> {
        return twitchRepository.getRecentChannels()
            .mapNotNull { channels ->
                PagingData.from(
                    channels.orEmpty(),
                    LoadStates(
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.NotLoading(endOfPaginationReached = true),
                        refresh = LoadState.NotLoading(endOfPaginationReached = true),
                    ),
                )
            }
    }

    fun onQueryChange(query: String) {
        _state.update { state ->
            state.copy(query = query)
        }
    }

    fun onSearchActiveChange(isActive: Boolean) {
        _state.update { state ->
            state.copy(
                isActive = isActive,
                query = if (isActive) state.query else "",
            )
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
