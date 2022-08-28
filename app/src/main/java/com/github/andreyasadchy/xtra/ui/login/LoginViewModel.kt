package com.github.andreyasadchy.xtra.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.repository.AuthPreferencesRepository
import com.github.andreyasadchy.xtra.repository.AuthRepository
import com.github.andreyasadchy.xtra.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    sealed class State {
        object Initial : State()
        data class LoadWebView(
            val clientId: String,
            val redirect: String,
            val exception: Exception? = null
        ) : State()

        object Done : State()
    }

    private val _state = MutableStateFlow<State>(State.Initial)
    val state: LiveData<State> = _state.asLiveData()

    fun onStart() {
        viewModelScope.launch {
            val user = userPreferencesRepository.user.first()
            if (user !is User.NotLoggedIn) {
                userPreferencesRepository.updateUser(null)

                try {
                    val token = user.helixToken
                    if (!token.isNullOrBlank()) {
                        authRepository.revokeToken()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            _state.update {
                State.LoadWebView(
                    clientId = authPreferencesRepository.helixClientId.first(),
                    redirect = authPreferencesRepository.helixRedirect.first()
                )
            }
        }
    }

    fun onTokenReceived(token: String) {
        viewModelScope.launch {
            try {
                val response = authRepository.validate(token) ?: throw IOException()
                userPreferencesRepository.updateUser(
                    user = User.LoggedIn(
                        id = response.userId,
                        login = response.login,
                        helixToken = token
                    )
                )
                _state.update { State.Done }
            } catch (e: Exception) {
                val state = _state.value as? State.LoadWebView ?: return@launch
                _state.update { state.copy(exception = e) }
            }
        }
    }
}
