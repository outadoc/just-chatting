package fr.outadoc.justchatting.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.model.AppUser
import fr.outadoc.justchatting.model.id.ValidationResponse
import fr.outadoc.justchatting.repository.AuthRepository
import fr.outadoc.justchatting.repository.PreferenceRepository
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.util.Event
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel(
    private val repository: TwitchService,
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
            .onEach { Log.d("MainViewModel", it.toString()) }
            .map { prefs ->
                when (prefs.appUser) {
                    is AppUser.LoggedIn -> State.LoggedIn(appUser = prefs.appUser)
                    AppUser.NotLoggedIn -> State.LoggedOut()
                    is AppUser.NotValidated -> {
                        try {
                            val userInfo: ValidationResponse = authRepository.validate()
                                ?: throw IllegalStateException("401")

                            val validatedUser = AppUser.LoggedIn(
                                id = userInfo.userId,
                                login = userInfo.login,
                                helixToken = prefs.appUser.helixToken
                            )

                            if (userInfo.clientId != prefs.helixClientId) {
                                throw IllegalStateException("401")
                            }

                            preferencesRepository.updatePreferences { current ->
                                current.copy(appUser = validatedUser)
                            }

                            State.LoggedIn(appUser = validatedUser)

                        } catch (e: Exception) {
                            if ((e is IllegalStateException && e.message == "401") || (e is HttpException && e.code() == 401)) {
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
                AppUser.NotLoggedIn -> Destination.Login()
                is AppUser.NotValidated -> {
                    try {
                        val userInfo: ValidationResponse = authRepository.validate()
                            ?: throw IllegalStateException("401")

                        val validatedUser = AppUser.LoggedIn(
                            id = userInfo.userId,
                            login = userInfo.login,
                            helixToken = prefs.appUser.helixToken
                        )

                        if (userInfo.clientId != prefs.helixClientId) {
                            throw IllegalStateException("401")
                        }

                        preferencesRepository.updatePreferences { current ->
                            current.copy(appUser = validatedUser)
                        }

                        null

                    } catch (e: Exception) {
                        if ((e is IllegalStateException && e.message == "401") || (e is HttpException && e.code() == 401)) {
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
                repository.loadUsersByLogin(listOf(login))
                    ?.firstOrNull()
                    ?.let { user ->
                        onViewChannelRequest(
                            id = user.id,
                            login = user.login,
                            name = user.displayName,
                            channelLogo = user.profileImageUrl
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
            Event(
                Destination.Channel(login = login)
            )
        )
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
