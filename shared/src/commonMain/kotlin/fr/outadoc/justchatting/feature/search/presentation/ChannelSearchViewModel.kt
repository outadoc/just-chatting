package fr.outadoc.justchatting.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import fr.outadoc.justchatting.feature.search.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

internal class ChannelSearchViewModel(
    private val twitchRepository: TwitchRepository,
) : ViewModel() {

    data class State(
        val query: String = "",
        val isSearchExpanded: Boolean = false,
        val recentChannels: ImmutableList<User> = persistentListOf(),
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pagingData: Flow<PagingData<ChannelSearchResult>> =
        state.mapNotNull { state -> state.query }
            .distinctUntilChanged()
            .debounce(0.3.seconds)
            .flatMapLatest { query ->
                if (query.isNotEmpty()) {
                    twitchRepository.searchChannels(query)
                } else {
                    flowOf(
                        PagingData.empty(
                            sourceLoadStates = LoadStates(
                                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                                append = LoadState.NotLoading(endOfPaginationReached = true),
                                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                            ),
                        ),
                    )
                }
            }
            .cachedIn(viewModelScope)

    fun onStart() {
        viewModelScope.launch {
            twitchRepository
                .getRecentChannels()
                .collect { users ->
                    _state.update { state ->
                        state.copy(
                            recentChannels = users.toPersistentList(),
                        )
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _state.update { state ->
            state.copy(query = query)
        }
    }

    fun onSearchExpandedChange(isExpanded: Boolean) {
        _state.update { state ->
            state.copy(
                isSearchExpanded = isExpanded,
                query = if (isExpanded) state.query else "",
            )
        }
    }

    fun onDismissSearchBar() {
        _state.update { state ->
            state.copy(isSearchExpanded = false)
        }
    }

    fun onClearSearchBar() {
        _state.update { state ->
            state.copy(query = "")
        }
    }
}
