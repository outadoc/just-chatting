package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.presentation.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

internal class StreamAndUserInfoViewModel(
    private val twitchRepository: TwitchRepository,
) : ViewModel() {

    sealed class State {
        data object Loading : State()
        data class Loaded(
            val user: User?,
            val stream: Stream?,
        ) : State()
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            combine(
                twitchRepository.getUserById(userId),
                twitchRepository.getStreamByUserId(userId),
            ) { user, stream -> user to stream }
                .map { (userResult, streamResult) ->
                    val user: User? = userResult
                        .onFailure { exception ->
                            logError<StreamAndUserInfoViewModel>(exception) {
                                "Error while loading user $userId: $exception"
                            }
                        }
                        .getOrNull()

                    val stream: Stream? = streamResult
                        .onFailure { exception ->
                            logError<StreamAndUserInfoViewModel>(exception) {
                                "Error while loading stream for user $userId: $exception"
                            }
                        }
                        .getOrNull()

                    State.Loaded(
                        user = user,
                        stream = stream,
                    )
                }
                .onStart<State> {
                    emit(State.Loading)
                }
                .collect { state ->
                    _state.emit(state)
                }
        }
    }
}
