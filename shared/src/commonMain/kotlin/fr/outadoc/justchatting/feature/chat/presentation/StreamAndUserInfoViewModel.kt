package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import fr.outadoc.justchatting.component.chatapi.domain.repository.TwitchRepository
import fr.outadoc.justchatting.lifecycle.ViewModel
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StreamAndUserInfoViewModel(
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
                .loadUserByLogin(login)
                .fold(
                    onSuccess = { user ->
                        _state.emit(
                            State.Loaded(
                                userLogin = login,
                                user = user,
                                stream = null,
                            ),
                        )

                        twitchRepository.loadStream(userId = user.id)
                            .fold(
                                onSuccess = { stream ->
                                    _state.emit(
                                        State.Loaded(
                                            userLogin = login,
                                            user = user,
                                            stream = stream,
                                        ),
                                    )
                                },
                                onFailure = { e ->
                                    logError<StreamAndUserInfoViewModel>(e) { "Error while loading stream for $login" }
                                    _state.emit(
                                        State.Error(e),
                                    )
                                },
                            )
                    },
                    onFailure = { e ->
                        logError<StreamAndUserInfoViewModel>(e) { "Error while loading user info for $login" }
                        _state.emit(
                            State.Error(e),
                        )
                    },
                )
        }
    }
}
