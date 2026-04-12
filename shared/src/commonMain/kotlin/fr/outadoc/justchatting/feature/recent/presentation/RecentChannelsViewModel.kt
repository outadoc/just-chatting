package fr.outadoc.justchatting.feature.recent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.shared.domain.model.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

public class RecentChannelsViewModel internal constructor(
    private val repository: TwitchRepository,
) : ViewModel() {
    public sealed class State {
        public data object Loading : State()

        public data class Content(
            val data: ImmutableList<User>,
        ) : State()
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    public val state: StateFlow<State> = _state.asStateFlow()

    private var job: Job? = null

    public fun refresh() {
        job?.cancel()
        job =
            viewModelScope.launch {
                _state.emitAll(
                    repository
                        .getRecentChannels()
                        .map { channels ->
                            State.Content(channels.toPersistentList())
                        },
                )
            }
    }
}
