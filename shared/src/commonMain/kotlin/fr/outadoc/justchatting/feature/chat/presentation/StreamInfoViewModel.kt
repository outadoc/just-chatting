package fr.outadoc.justchatting.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

internal class StreamInfoViewModel(
    private val twitchRepository: TwitchRepository,
) : ViewModel() {

    sealed class State {
        data object Loading : State()
        data class Error(val throwable: Throwable) : State()
        data class Loaded(val stream: Stream) : State()
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            twitchRepository
                .getStreamByUserId(userId)
                .map { streamResult ->
                    streamResult
                        .fold(
                            onSuccess = { stream ->
                                State.Loaded(stream)
                            },
                            onFailure = { exception ->
                                logError<StreamInfoViewModel>(exception) {
                                    "Error while loading stream for user $userId: $exception"
                                }
                                State.Error(exception)
                            }
                        )
                }
                .onStart { emit(State.Loading) }
                .collect(_state)
        }
    }
}
