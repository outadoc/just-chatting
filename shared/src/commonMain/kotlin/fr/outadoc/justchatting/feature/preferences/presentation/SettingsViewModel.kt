package fr.outadoc.justchatting.feature.preferences.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.DetailScreen
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
public class SettingsViewModel internal constructor(
    private val preferenceRepository: PreferenceRepository,
    private val logRepository: LogRepository,
    private val twitchRepository: TwitchRepository,
    private val authRepository: AuthRepository,
    private val appVersionNameProvider: AppVersionNameProvider,
    private val appUpdateChecker: AppUpdateChecker,
    private val dispatchersProvider: DispatchersProvider,
) : ViewModel() {
    public sealed class Event {
        public data class ShareLogs(
            val uri: Uri,
        ) : Event()

        public data class NavigateToDetail(
            val screen: DetailScreen,
        ) : Event()
    }

    public data class State(
        val appPreferences: AppPreferences = AppPreferences(),
        val appVersionName: String? = null,
        val user: User? = null,
        val isUpdateCheckSupported: Boolean = false,
        val updateState: AppUpdateState = AppUpdateState(),
    )

    private val _events = MutableSharedFlow<Event>()
    public val events: SharedFlow<Event> = _events.asSharedFlow()

    public val state: StateFlow<State> =
        combine(
            preferenceRepository.currentPreferences,
            authRepository
                .currentUser
                .flatMapLatest { appUser ->
                    when (appUser) {
                        is AppUser.LoggedIn -> {
                            twitchRepository
                                .getUserById(appUser.userId)
                                .map { result ->
                                    result.fold(
                                        onSuccess = { user -> user },
                                        onFailure = { exception ->
                                            logError<SettingsViewModel>(exception) { "Failed to fetch user" }
                                            null
                                        },
                                    )
                                }
                        }

                        AppUser.NotLoggedIn -> {
                            flowOf(null)
                        }
                    }
                },
            appUpdateChecker.state,
        ) { prefs, user, updateState ->
            State(
                appPreferences = prefs,
                user = user,
                appVersionName = appVersionNameProvider.appVersionName,
                isUpdateCheckSupported = appUpdateChecker.isSupported,
                updateState = updateState,
            )
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = State(),
        )

    public fun onNavigateToDetail(screen: DetailScreen) {
        viewModelScope.launch {
            _events.emit(Event.NavigateToDetail(screen))
        }
    }

    public fun updatePreferences(appPreferences: AppPreferences) {
        viewModelScope.launch(dispatchersProvider.io) {
            preferenceRepository.updatePreferences { appPreferences }
        }
    }

    public fun logout() {
        viewModelScope.launch(dispatchersProvider.io) {
            authRepository.logout()
        }
    }

    public fun onShareLogsClick() {
        viewModelScope.launch {
            try {
                if (logRepository.isSupported) {
                    _events.emit(
                        Event.ShareLogs(
                            uri = logRepository.dumpLogs(),
                        ),
                    )
                }
            } catch (e: Exception) {
                logError<SettingsViewModel>(e) { "Error while reading logs" }
            }
        }
    }

    public fun checkForUpdates() {
        viewModelScope.launch {
            if (appUpdateChecker.isSupported) {
                appUpdateChecker.checkForUpdate()
            }
        }
    }

    public fun installUpdate() {
        viewModelScope.launch {
            appUpdateChecker.installUpdate()
        }
    }
}
