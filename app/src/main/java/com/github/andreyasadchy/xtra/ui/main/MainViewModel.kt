package com.github.andreyasadchy.xtra.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.asLoggedIn
import com.github.andreyasadchy.xtra.repository.AuthPreferencesRepository
import com.github.andreyasadchy.xtra.repository.AuthRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repository: TwitchService,
    private val authRepository: AuthRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    sealed class State {
        object Loading : State()
        data class Loaded(val destination: Destination) : State()
    }

    sealed class Destination {
        object Home : Destination()

        object Search : Destination()

        object Settings : Destination()

        data class Login(
            val causedByTokenExpiration: Boolean = false
        ) : Destination()

        data class Channel(
            val id: String,
            val login: String,
            val name: String,
            val channelLogo: String
        ) : Destination()
    }

    private val _userRequestedDestination = MutableSharedFlow<Destination>(replay = 1)

    private val _state = flow {
        emit(State.Loading)
        combine(
            userPreferencesRepository.user,
            authPreferencesRepository.helixClientId
        ) { user, helixClientId ->
            when (user) {
                is User.LoggedIn -> {
                    emit(State.Loaded(Destination.Home))
                }
                User.NotLoggedIn -> {
                    emit(State.Loaded(Destination.Login()))
                }
                is User.NotValidated -> {
                    try {
                        val token = user.helixToken
                        if (!token.isNullOrBlank()) {
                            val response = authRepository.validate(token)
                            val validatedUser = user.asLoggedIn()
                            if (response?.clientId == helixClientId && validatedUser != null) {
                                userPreferencesRepository.updateUser(validatedUser)
                            } else {
                                throw IllegalStateException("401")
                            }
                        }
                    } catch (e: Exception) {
                        if ((e is IllegalStateException && e.message == "401") || (e is HttpException && e.code() == 401)) {
                            userPreferencesRepository.updateUser(null)
                            emit(
                                State.Loaded(
                                    Destination.Login(causedByTokenExpiration = true)
                                )
                            )
                        }
                    }
                }
            }

            emitAll(_userRequestedDestination.map { State.Loaded(it) })
        }.collect()
    }

    val state: LiveData<State> = _state.asLiveData()

    fun onViewChannelRequest(login: String) {
        viewModelScope.launch {
            try {
                repository.loadUsersByLogin(listOf(login))
                    ?.firstOrNull()
                    ?.let { user ->
                        onViewChannelRequest(
                            id = user.id,
                            login = user.login,
                            name = user.display_name,
                            channelLogo = user.channelLogo
                        )
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onViewChannelRequest(id: String?, login: String?, name: String?, channelLogo: String?) {
        if (id == null || login == null || name == null || channelLogo == null) return
        _userRequestedDestination.tryEmit(
            Destination.Channel(
                id = id,
                login = login,
                name = name,
                channelLogo = channelLogo
            )
        )
    }

    fun onOpenSearchRequested() {
        _userRequestedDestination.tryEmit(Destination.Search)
    }

    fun onOpenSettingsRequested() {
        _userRequestedDestination.tryEmit(Destination.Settings)
    }
}
