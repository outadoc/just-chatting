package fr.outadoc.justchatting.feature.home.presentation

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.component.chatapi.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.component.chatapi.domain.model.ValidationResponse
import fr.outadoc.justchatting.component.chatapi.domain.repository.AuthRepository
import fr.outadoc.justchatting.component.deeplink.Deeplink
import fr.outadoc.justchatting.component.deeplink.DeeplinkParser
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.lifecycle.ViewModel
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.logging.logInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MainRouterViewModel(
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferenceRepository,
    private val deeplinkParser: DeeplinkParser,
    private val oAuthAppCredentials: OAuthAppCredentials,
) : ViewModel() {

    private class InvalidClientIdException : Exception()

    sealed class State {
        data object Loading : State()
        data object LoggedOut : State()
        data class LoggedIn(
            val appUser: AppUser.LoggedIn,
        ) : State()
    }

    sealed class Event {
        data class ViewChannel(val login: String) : Event()
        data class OpenInBrowser(val uri: String) : Event()
    }

    val state: StateFlow<State> =
        preferencesRepository.currentPreferences
            .map { prefs ->
                when (val appUser = prefs.appUser) {
                    is AppUser.LoggedIn -> State.LoggedIn(appUser = appUser)
                    is AppUser.NotLoggedIn -> State.LoggedOut
                    is AppUser.NotValidated -> State.Loading
                }
            }
            .stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = State.Loading,
            )

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    fun onStart() {
        viewModelScope.launch {
            preferencesRepository.currentPreferences.collect { prefs ->
                if (prefs.appUser is AppUser.NotValidated) {
                    try {
                        val userInfo: ValidationResponse =
                            authRepository.validate(prefs.appUser.token)
                                ?: throw InvalidClientIdException()

                        val validatedUser = AppUser.LoggedIn(
                            userId = userInfo.userId,
                            userLogin = userInfo.login,
                            token = prefs.appUser.token,
                        )

                        if (userInfo.clientId != oAuthAppCredentials.clientId) {
                            throw InvalidClientIdException()
                        }

                        preferencesRepository.updatePreferences { current ->
                            current.copy(appUser = validatedUser)
                        }
                    } catch (e: Exception) {
                        logError<MainRouterViewModel>(e) { "Failed to validate token" }

                        preferencesRepository.updatePreferences { current ->
                            current.copy(appUser = AppUser.NotLoggedIn)
                        }
                    }
                }
            }
        }
    }

    fun onLoginClick() = viewModelScope.launch {
        val helixScopes = listOf(
            "chat:read",
            "chat:edit",
            "user:read:follows",
        )

        val helixAuthUrl: Uri =
            Uri.parse("https://id.twitch.tv/oauth2/authorize")
                .buildUpon()
                .appendQueryParameter("response_type", "token")
                .appendQueryParameter("client_id", oAuthAppCredentials.clientId)
                .appendQueryParameter("redirect_uri", oAuthAppCredentials.redirectUri.toString())
                .appendQueryParameter("force_verify", "true")
                .appendQueryParameter("scope", helixScopes.joinToString(" "))
                .build()

        _events.emit(
            Event.OpenInBrowser(uri = helixAuthUrl.toString()),
        )
    }

    fun onReceiveIntent(uri: String) = viewModelScope.launch {
        val deeplink = deeplinkParser.parseDeeplink(uri)

        logInfo<MainRouterViewModel> { "Received deeplink $deeplink" }

        when (deeplink) {
            is Deeplink.Authenticated -> {
                preferencesRepository.updatePreferences { prefs ->
                    prefs.copy(
                        appUser = AppUser.NotValidated(
                            token = deeplink.token,
                        ),
                    )
                }

                // Artificial delay to ensure Ktor has time to get the memo about the new token
                delay(1.seconds)
            }

            is Deeplink.ViewChannel -> {
                _events.emit(
                    Event.ViewChannel(login = deeplink.login),
                )
            }

            null -> {
                logError<MainRouterViewModel> { "Invalid deeplink: $uri" }
            }
        }
    }
}
