package fr.outadoc.justchatting.feature.shared.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.auth.data.AuthCallbackWebServer
import fr.outadoc.justchatting.feature.deeplink.Deeplink
import fr.outadoc.justchatting.feature.deeplink.DeeplinkParser
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.logging.logInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

internal class MainRouterViewModel(
    private val authRepository: AuthRepository,
    private val deeplinkParser: DeeplinkParser,
    private val authCallbackWebServer: AuthCallbackWebServer,
) : ViewModel(),
    DeeplinkReceiver {

    sealed class State {
        data object Loading : State()
        data object LoggedOut : State()
        data class LoggedIn(
            val appUser: AppUser.LoggedIn,
        ) : State()
    }

    sealed class Event {
        data class ViewChannel(val userId: String) : Event()
        data class ShowAuthPage(val uri: Uri) : Event()
    }

    val state: StateFlow<State> =
        authRepository.currentUser
            .map { appUser ->
                when (appUser) {
                    is AppUser.LoggedIn -> State.LoggedIn(appUser = appUser)
                    is AppUser.NotLoggedIn -> State.LoggedOut
                }
            }
            .onEach { state ->
                when (state) {
                    is State.LoggedOut -> {
                        authCallbackWebServer.start()
                    }

                    is State.Loading,
                    is State.LoggedIn,
                    -> {
                        authCallbackWebServer.stop()
                    }
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
            authCallbackWebServer.receivedUris.collect { uri ->
                onDeeplinkReceived(uri)
            }
        }
    }

    fun onLoginClick() = viewModelScope.launch {
        _events.emit(
            Event.ShowAuthPage(
                uri = authRepository.getExternalAuthorizeUrl(),
            ),
        )
    }

    override fun onDeeplinkReceived(uriString: String) {
        onDeeplinkReceived(Uri.parse(uriString))
    }

    override fun onDeeplinkReceived(uri: Uri) {
        viewModelScope.launch {
            val deeplink = deeplinkParser.parseDeeplink(uri)

            logInfo<MainRouterViewModel> { "Received deeplink $deeplink" }

            when (deeplink) {
                is Deeplink.Authenticated -> {
                    authRepository.saveToken(deeplink.token)

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
}
