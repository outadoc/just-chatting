package fr.outadoc.justchatting.ui.view.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.ui.common.BaseViewModel
import kotlinx.coroutines.launch

class StreamInfoViewModel(private val repository: TwitchService) : BaseViewModel() {

    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Loaded(val user: User, val stream: Stream?) : State()
        data class Error(val error: Throwable) : State()
    }

    private val _state = MutableLiveData<State>(State.Idle)
    val state: LiveData<State> = _state

    fun loadUser(channelId: String) {
        if (_state.value is State.Loading) return
        _state.value = State.Loading

        viewModelScope.launch {
            try {
                val user = repository.loadUsersById(
                    ids = mutableListOf(channelId)
                )?.firstOrNull()

                user ?: error("Failed to load the user.")

                val stream = repository.loadStreamWithUser(
                    channelId = channelId
                )

                _state.value = State.Loaded(user = user, stream = stream)
            } catch (e: Exception) {
                _state.value = State.Error(e)
            }
        }
    }
}
