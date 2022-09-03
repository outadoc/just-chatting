package fr.outadoc.justchatting.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.model.User
import fr.outadoc.justchatting.model.asLoggedIn
import fr.outadoc.justchatting.repository.AuthPreferencesRepository
import fr.outadoc.justchatting.repository.AuthRepository
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.repository.UserPreferencesRepository
import fr.outadoc.justchatting.util.Event
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel(
    private val repository: TwitchService,
    private val authRepository: AuthRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    sealed class Destination {

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

    private val _forcedLoginDestination: Flow<Destination?> =
        combine(
            userPreferencesRepository.user,
            authPreferencesRepository.helixClientId
        ) { user, helixClientId ->
            when (user) {
                is User.LoggedIn -> null
                User.NotLoggedIn -> Destination.Login()
                is User.NotValidated -> {
                    try {
                        val token = user.helixToken
                        if (token.isNullOrBlank()) null
                        else {
                            val response = authRepository.validate(token)
                            val validatedUser = user.asLoggedIn()
                            if (response?.clientId == helixClientId && validatedUser != null) {
                                userPreferencesRepository.updateUser(validatedUser)
                                null
                            } else {
                                throw IllegalStateException("401")
                            }
                        }
                    } catch (e: Exception) {
                        if ((e is IllegalStateException && e.message == "401") || (e is HttpException && e.code() == 401)) {
                            userPreferencesRepository.updateUser(null)
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
            Event(
                Destination.Channel(
                    id = id,
                    login = login,
                    name = name,
                    channelLogo = channelLogo
                )
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
