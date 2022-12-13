package fr.outadoc.justchatting.feature.mainnavigation.presentation

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.component.deeplink.Deeplink
import fr.outadoc.justchatting.component.deeplink.DeeplinkParser
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.component.twitch.domain.repository.AuthRepository
import fr.outadoc.justchatting.component.twitch.model.OAuthAppCredentials
import fr.outadoc.justchatting.component.twitch.model.ValidationResponse
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel(
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferenceRepository,
    private val deeplinkParser: DeeplinkParser,
    private val oAuthAppCredentials: OAuthAppCredentials
) : ViewModel() {

    private class InvalidClientIdException : Exception()

    sealed class State {
        object Loading : State()
        data class LoggedOut(
            val causedByTokenExpiration: Boolean = false
        ) : State()

        data class LoggedIn(val appUser: AppUser) : State()
    }

    sealed class Event {
        data class ViewChannel(val login: String) : Event()
        data class OpenInBrowser(val uri: Uri) : Event()
    }

    val state: StateFlow<State> =
        preferencesRepository.currentPreferences
            .map { prefs ->
                when (val appUser = prefs.appUser) {
                    is AppUser.LoggedIn -> State.LoggedIn(appUser = appUser)
                    is AppUser.NotLoggedIn -> State.LoggedOut()
                    is AppUser.NotValidated -> {
                        try {
                            val userInfo: ValidationResponse = authRepository.validate()
                                ?: throw InvalidClientIdException()

                            val validatedUser = AppUser.LoggedIn(
                                id = userInfo.userId,
                                login = userInfo.login,
                                helixToken = appUser.helixToken
                            )

                            if (userInfo.clientId != oAuthAppCredentials.clientId) {
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

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    fun onLoginClick() = viewModelScope.launch {
        val helixScopes = listOf(
            "chat:read",
            "chat:edit",
            "channel:moderate",
            "user:read:follows"
        )

        val helixAuthUrl: Uri =
            "https://id.twitch.tv/oauth2/authorize".toUri()
                .buildUpon()
                .appendQueryParameter("response_type", "token")
                .appendQueryParameter("client_id", oAuthAppCredentials.clientId)
                .appendQueryParameter("redirect_uri", oAuthAppCredentials.redirectUri.toString())
                .appendQueryParameter("force_verify", "true")
                .appendQueryParameter("scope", helixScopes.joinToString(" "))
                .build()

        _events.emit(
            Event.OpenInBrowser(uri = helixAuthUrl)
        )
    }

    fun onReceiveIntent(data: Uri) = viewModelScope.launch {
        when (val deeplink = deeplinkParser.parseDeeplink(data)) {
            is Deeplink.Authenticated -> {
                preferencesRepository.updatePreferences { prefs ->
                    prefs.copy(
                        appUser = AppUser.NotValidated(
                            helixToken = deeplink.token
                        )
                    )
                }
            }

            is Deeplink.ViewChannel -> {
                _events.emit(
                    Event.ViewChannel(login = deeplink.login)
                )
            }

            null -> {
                logError<MainViewModel> { "Invalid deeplink: $data" }
            }
        }
    }
}
