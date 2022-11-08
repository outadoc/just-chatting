package fr.outadoc.justchatting.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.model.AppUser
import fr.outadoc.justchatting.model.id.ValidationResponse
import fr.outadoc.justchatting.repository.AuthRepository
import fr.outadoc.justchatting.repository.InvalidClientIdException
import fr.outadoc.justchatting.repository.PreferenceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import retrofit2.HttpException

class MainViewModel(
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferenceRepository
) : ViewModel() {

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
}
