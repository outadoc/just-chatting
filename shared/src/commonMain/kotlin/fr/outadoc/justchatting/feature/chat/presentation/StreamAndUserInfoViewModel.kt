package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.home.domain.TwitchRepository
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.User
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
        data object Initial : State()

        data class Loading(
            val userLogin: String,
        ) : State()

        data class Loaded(
            val userLogin: String,
            val user: User?,
            val stream: Stream?,
        ) : State()
    }

    private val _state = MutableStateFlow<State>(State.Initial)
    val state = _state.asStateFlow()

    fun loadFromLogin(login: String) {
        viewModelScope.launch {
            combine(
                twitchRepository.getUserByLogin(login),
                twitchRepository.getStreamByUserLogin(login),
            ) { user, stream -> user to stream }
                .map { (userResult, streamResult) ->
                    val user: User? = userResult
                        .onFailure { exception ->
                            logError<StreamAndUserInfoViewModel>(exception) {
                                "Error while loading user ${login}: $exception"
                            }
                        }
                        .getOrNull()

                    val stream: Stream? = streamResult
                        .onFailure { exception ->
                            logError<StreamAndUserInfoViewModel>(exception) {
                                "Error while loading stream for ${login}: $exception"
                            }
                        }
                        .getOrNull()

                    State.Loaded(
                        userLogin = login,
                        user = user,
                        stream = stream,
                    )
                }
                .onStart<State> {
                    emit(State.Loading(userLogin = login))
                }
                .collect { state ->
                    _state.emit(state)
                }
        }
    }
}
