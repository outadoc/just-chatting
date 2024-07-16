package fr.outadoc.justchatting.feature.preferences.presentation

import fr.outadoc.justchatting.feature.auth.domain.AuthRepository
import fr.outadoc.justchatting.feature.home.domain.TwitchRepository
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.presentation.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
internal class SettingsViewModel(
    private val preferenceRepository: PreferenceRepository,
    private val authRepository: AuthRepository,
    private val logRepository: LogRepository,
    private val twitchRepository: TwitchRepository,
) : ViewModel() {

    sealed class Event {
        data class ShareLogs(val uri: String) : Event()
    }

    data class State(
        val appPreferences: AppPreferences = AppPreferences(),
        val user: User? = null,
    )

    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _events.asSharedFlow()

    val state: StateFlow<State> =
        preferenceRepository
            .currentPreferences
            .flatMapLatest { prefs ->
                when (prefs.appUser) {
                    is AppUser.LoggedIn -> {
                        twitchRepository
                            .getUserById(prefs.appUser.userId)
                            .map { user ->
                                State(
                                    appPreferences = prefs,
                                    user = user.getOrNull(),
                                )
                            }
                    }

                    else -> {
                        flowOf(
                            State(appPreferences = prefs),
                        )
                    }
                }
            }
            .stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = State(),
            )

    fun updatePreferences(appPreferences: AppPreferences) {
        viewModelScope.launch(DispatchersProvider.io) {
            preferenceRepository.updatePreferences { appPreferences }
        }
    }

    fun logout() {
        viewModelScope.launch(DispatchersProvider.io) {
            preferenceRepository.updatePreferences { current ->
                current.copy(appUser = AppUser.NotLoggedIn)
            }

            authRepository
                .revokeToken()
                .onFailure { exception ->
                    logError<SettingsViewModel>(exception) { "Failed to revoke token" }
                }
        }
    }

    fun onShareLogsClick() {
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
}
