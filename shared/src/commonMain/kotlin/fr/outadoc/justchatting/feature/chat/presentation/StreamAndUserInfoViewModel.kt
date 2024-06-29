package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.home.domain.TwitchRepository
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.presentation.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            val user: User,
            val stream: Stream?,
        ) : State()

        data class Error(
            val throwable: Throwable?,
        ) : State()
    }

    private val _state = MutableStateFlow<State>(State.Initial)
    val state = _state.asStateFlow()

    fun loadFromLogin(login: String) {
        viewModelScope.launch {
            _state.emit(State.Loading(userLogin = login))

            twitchRepository
                .getUserByLogin(login)
                .fold(
                    onSuccess = { user ->
                        _state.emit(
                            State.Loaded(
                                userLogin = login,
                                user = user,
                                stream = null,
                            ),
                        )

                        twitchRepository.getStream(userId = user.id)
                            .onSuccess { stream ->
                                _state.emit(
                                    State.Loaded(
                                        userLogin = login,
                                        user = user,
                                        stream = stream,
                                    ),
                                )
                            }
                            .onFailure { exception ->
                                logError<StreamAndUserInfoViewModel>(exception) { "Error while loading stream for $login" }
                            }
                    },
                    onFailure = { exception ->
                        logError<StreamAndUserInfoViewModel>(exception) { "Error while loading user info for $login" }
                        _state.emit(
                            State.Error(exception),
                        )
                    },
                )
        }
    }
}
