package fr.outadoc.justchatting.feature.preferences.presentation

import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.presentation.ViewModel
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
internal class SettingsViewModel(
    private val preferenceRepository: PreferenceRepository,
    private val logRepository: LogRepository,
    private val twitchRepository: TwitchRepository,
    private val authRepository: AuthRepository,
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
        ) { prefs, user ->
            State(
                appPreferences = prefs,
                user = user,
            )
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
            authRepository.logout()
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
