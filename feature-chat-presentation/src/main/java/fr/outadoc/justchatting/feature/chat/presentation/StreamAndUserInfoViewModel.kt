package fr.outadoc.justchatting.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import fr.outadoc.justchatting.component.chatapi.domain.repository.TwitchRepository
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StreamAndUserInfoViewModel(
    private val twitchRepository: TwitchRepository,
) : ViewModel() {

    sealed class State {
        object Initial : State()

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
            try {
                _state.emit(State.Loading(userLogin = login))

                val user: User =
                    twitchRepository.loadUsersByLogin(logins = listOf(login))
                        ?.firstOrNull()
                        ?: error("API returned no user")

                _state.emit(
                    State.Loaded(
                        userLogin = login,
                        user = user,
                        stream = null,
                    ),
                )

                _state.emit(
                    State.Loaded(
                        userLogin = login,
                        user = user,
                        stream = twitchRepository.loadStream(userId = user.id),
                    ),
                )
            } catch (e: Exception) {
                logError<StreamAndUserInfoViewModel>(e) { "Error while loading stream + user info for $login" }
                _state.emit(
                    State.Error(e),
                )
            }
        }
    }
}
