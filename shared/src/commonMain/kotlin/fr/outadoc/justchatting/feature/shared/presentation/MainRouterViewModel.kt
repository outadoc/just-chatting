package fr.outadoc.justchatting.feature.shared.presentation

import fr.outadoc.justchatting.feature.auth.domain.AuthRepository
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.deeplink.Deeplink
import fr.outadoc.justchatting.feature.deeplink.DeeplinkParser
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.logging.logInfo
import fr.outadoc.justchatting.utils.presentation.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

internal class MainRouterViewModel(
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
        data class ViewChannel(val userId: String) : Event()
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
                    val appUser = authRepository
                        .validate(prefs.appUser.token)
                        .mapCatching { userInfo ->
                            val validatedUser = AppUser.LoggedIn(
                                userId = userInfo.userId,
                                userLogin = userInfo.login,
                                token = prefs.appUser.token,
                            )

                            if (userInfo.clientId != oAuthAppCredentials.clientId) {
                                throw InvalidClientIdException()
                            }

                            validatedUser
                        }
                        .fold(
                            onSuccess = { it },
                            onFailure = { exception ->
                                logError<MainRouterViewModel>(exception) { "Failed to validate token" }
                                AppUser.NotLoggedIn
                            },
                        )

                    preferencesRepository.updatePreferences { current ->
                        current.copy(appUser = appUser)
                    }
                }
            }
        }
    }

    fun onLoginClick() = viewModelScope.launch {
        _events.emit(
            Event.OpenInBrowser(
                uri = authRepository.getExternalAuthorizeUrl().toString(),
            ),
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
                    Event.ViewChannel(userId = deeplink.userId),
                )
            }

            null -> {
                logError<MainRouterViewModel> { "Invalid deeplink: $uri" }
            }
        }
    }
}
