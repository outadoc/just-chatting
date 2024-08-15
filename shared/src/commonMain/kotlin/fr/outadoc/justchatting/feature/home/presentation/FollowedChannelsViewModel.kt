package fr.outadoc.justchatting.feature.home.presentation

import fr.outadoc.justchatting.feature.home.domain.TwitchRepository
import fr.outadoc.justchatting.feature.home.domain.model.ChannelFollow
import fr.outadoc.justchatting.utils.presentation.ViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class FollowedChannelsViewModel(
    private val repository: TwitchRepository,
) : ViewModel() {

    sealed class State {
        data object Loading : State()
        data class Content(
            val data: List<ChannelFollow>
        ) : State()
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private var job: Job? = null

    fun refresh() {
        job?.cancel()
        job = viewModelScope.launch {
            repository
                .getFollowedChannels()
                .collect { channels ->
                    _state.value = State.Content(channels)
                }
        }
    }
}
