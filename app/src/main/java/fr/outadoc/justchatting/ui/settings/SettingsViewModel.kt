package fr.outadoc.justchatting.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.repository.AppPreferences
import fr.outadoc.justchatting.repository.PreferenceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    val appPreferences: StateFlow<AppPreferences> =
        preferenceRepository.currentPreferences.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = AppPreferences()
        )

    fun updatePreferences(appPreferences: AppPreferences) {
        viewModelScope.launch {
            preferenceRepository.updatePreferences(appPreferences)
        }
    }
}