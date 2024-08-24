package fr.outadoc.justchatting.feature.followed.presentation

import fr.outadoc.justchatting.feature.followed.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.recent.presentation.RecentChannelsViewModel.State
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.utils.presentation.ViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class FollowedChannelsViewModel(
    private val repository: TwitchRepository,
) : ViewModel() {

    sealed class State {
        data object Loading : State()
        data class Content(
            val data: ImmutableList<ChannelFollow>,
        ) : State()
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private var job: Job? = null

    fun refresh() {
        job?.cancel()
        job = viewModelScope.launch {
            _state.emitAll(
                repository
                    .getFollowedChannels()
                    .map { channels ->
                        State.Content(channels.toPersistentList())
                    },
            )
        }
    }
}
