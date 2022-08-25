package com.github.andreyasadchy.xtra.ui.view.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.model.helix.user.User
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class StreamInfoViewModel @Inject constructor(
    private val repository: TwitchService,
) : BaseViewModel() {

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
