package fr.outadoc.justchatting.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.model.User
import fr.outadoc.justchatting.model.asLoggedIn
import fr.outadoc.justchatting.repository.AuthPreferencesRepository
import fr.outadoc.justchatting.repository.AuthRepository
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.repository.UserPreferencesRepository
import fr.outadoc.justchatting.util.Event
import fr.outadoc.justchatting.util.asEventLiveData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel(
    private val repository: TwitchService,
    private val authRepository: AuthRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    sealed class State {
        object Loading : State()
        data class NavigateTo(val destination: Destination) : State()
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

    private val _events = flow {
        emit(State.Loading)
        combine(
            userPreferencesRepository.user,
            authPreferencesRepository.helixClientId
        ) { user, helixClientId ->
            when (user) {
                is User.LoggedIn -> {
                    emit(State.NavigateTo(Destination.Home))
                }
                User.NotLoggedIn -> {
                    emit(State.NavigateTo(Destination.Login()))
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
                                State.NavigateTo(
                                    Destination.Login(causedByTokenExpiration = true)
                                )
                            )
                        }
                    }
                }
            }

            emitAll(_userRequestedDestination.map { State.NavigateTo(it) })
        }.collect()
    }

    val events: LiveData<Event<State>> = _events.asEventLiveData()

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
