package fr.outadoc.justchatting.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.model.AppUser
import fr.outadoc.justchatting.model.id.ValidationResponse
import fr.outadoc.justchatting.repository.AuthRepository
import fr.outadoc.justchatting.repository.InvalidClientIdException
import fr.outadoc.justchatting.repository.PreferenceRepository
import fr.outadoc.justchatting.util.Event
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel(
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferenceRepository
) : ViewModel() {

    sealed class Destination {

        object Search : Destination()

        object Settings : Destination()

        data class Login(
            val causedByTokenExpiration: Boolean = false
        ) : Destination()

        data class Channel(val login: String) : Destination()
    }

    sealed class State {
        object Loading : State()
        data class LoggedOut(
            val causedByTokenExpiration: Boolean = false
        ) : State()

        data class LoggedIn(val appUser: AppUser) : State()
    }

    val state: StateFlow<State> =
        preferencesRepository.currentPreferences
            .map { prefs ->
                when (prefs.appUser) {
                    is AppUser.LoggedIn -> State.LoggedIn(appUser = prefs.appUser)
                    is AppUser.NotLoggedIn -> State.LoggedOut()
                    is AppUser.NotValidated -> {
                        try {
                            val userInfo: ValidationResponse = authRepository.validate()
                                ?: throw InvalidClientIdException()

                            val validatedUser = AppUser.LoggedIn(
                                id = userInfo.userId,
                                login = userInfo.login,
                                helixToken = prefs.appUser.helixToken
                            )

                            if (userInfo.clientId != prefs.helixClientId) {
                                throw InvalidClientIdException()
                            }

                            preferencesRepository.updatePreferences { current ->
                                current.copy(appUser = validatedUser)
                            }

                            State.LoggedIn(appUser = validatedUser)

                        } catch (e: Exception) {
                            if (e is InvalidClientIdException || (e as? HttpException)?.code() == 401) {
                                preferencesRepository.updatePreferences { current ->
                                    current.copy(appUser = AppUser.NotLoggedIn)
                                }
                                State.LoggedOut(causedByTokenExpiration = true)
                            } else {
                                State.LoggedOut()
                            }
                        }
                    }
                }
            }
            .stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = State.Loading
            )

    private val _forcedLoginDestination: Flow<Destination?> =
        preferencesRepository.currentPreferences.map { prefs ->
            when (prefs.appUser) {
                is AppUser.LoggedIn -> null
                is AppUser.NotLoggedIn -> Destination.Login()
                is AppUser.NotValidated -> {
                    try {
                        val userInfo: ValidationResponse = authRepository.validate()
                            ?: throw InvalidClientIdException()

                        val validatedUser = AppUser.LoggedIn(
                            id = userInfo.userId,
                            login = userInfo.login,
                            helixToken = prefs.appUser.helixToken
                        )

                        if (userInfo.clientId != prefs.helixClientId) {
                            throw InvalidClientIdException()
                        }

                        preferencesRepository.updatePreferences { current ->
                            current.copy(appUser = validatedUser)
                        }

                        null

                    } catch (e: Exception) {
                        if (e is InvalidClientIdException || (e as? HttpException)?.code() == 401) {
                            preferencesRepository.updatePreferences { current ->
                                current.copy(appUser = AppUser.NotLoggedIn)
                            }
                            Destination.Login(causedByTokenExpiration = true)
                        } else {
                            null
                        }
                    }
                }
            }
        }

    private val _userRequestedDestination =
        MutableSharedFlow<Event<Destination>>(replay = 1)

    @OptIn(FlowPreview::class)
    val events: LiveData<Event<Destination>> =
        _forcedLoginDestination
            .flatMapMerge { forcedDestination ->
                forcedDestination?.let { flowOf(Event(it)) }
                    ?: _userRequestedDestination
            }
            .asLiveData()

    fun onViewChannelRequest(login: String) {
        viewModelScope.launch {
            try {
                _userRequestedDestination.tryEmit(
                    Event(
                        Destination.Channel(login = login)
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onOpenSearchRequested() {
        _userRequestedDestination.tryEmit(
            Event(Destination.Search)
        )
    }

    fun onOpenSettingsRequested() {
        _userRequestedDestination.tryEmit(
            Event(Destination.Settings)
        )
    }
}
