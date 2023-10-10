package fr.outadoc.justchatting.feature.preferences.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eygraber.uri.Uri
import fr.outadoc.justchatting.component.chatapi.domain.repository.AuthRepository
import fr.outadoc.justchatting.component.preferences.data.AppPreferences
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

class SettingsViewModel(
    private val preferenceRepository: PreferenceRepository,
    private val authRepository: AuthRepository,
    private val logRepository: LogRepository,
) : ViewModel() {

    sealed class Event {
        data class ShareLogs(val uri: Uri) : Event()
    }

    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _events.asSharedFlow()

    val appPreferences: StateFlow<AppPreferences> =
        preferenceRepository.currentPreferences.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = AppPreferences(),
        )

    fun updatePreferences(appPreferences: AppPreferences) {
        viewModelScope.launch {
            preferenceRepository.updatePreferences { appPreferences }
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferenceRepository.updatePreferences { current ->
                current.copy(appUser = AppUser.NotLoggedIn)
            }

            try {
                authRepository.revokeToken()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onShareLogsClick() {
        viewModelScope.launch {
            try {
                val file: Uri = logRepository.dumpLogs()
                _events.emit(Event.ShareLogs(uri = file))
            } catch (e: IOException) {
                logError<SettingsViewModel>(e) { "Error while reading logs" }
            }
        }
    }
}
