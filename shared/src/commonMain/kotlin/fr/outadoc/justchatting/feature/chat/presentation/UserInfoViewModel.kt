package fr.outadoc.justchatting.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

internal class UserInfoViewModel(
    private val twitchRepository: TwitchRepository,
) : ViewModel() {

    sealed class State {
        data object Loading : State()
        data class Error(val throwable: Throwable) : State()
        data class Loaded(val user: User) : State()
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            twitchRepository
                .getUserById(userId)
                .map { userResult ->
                    userResult.fold(
                        onSuccess = { user ->
                            State.Loaded(user)
                        },
                        onFailure = { exception ->
                            logError<UserInfoViewModel>(exception) {
                                "Error while loading user $userId: $exception"
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
