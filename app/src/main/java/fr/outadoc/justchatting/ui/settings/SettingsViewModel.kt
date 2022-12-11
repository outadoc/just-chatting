package fr.outadoc.justchatting.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.component.preferences.AppUser
import fr.outadoc.justchatting.component.preferences.AppPreferences
import fr.outadoc.justchatting.component.preferences.PreferenceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferenceRepository: fr.outadoc.justchatting.component.preferences.PreferenceRepository,
    private val authRepository: fr.outadoc.justchatting.component.twitch.domain.repository.AuthRepository
) : ViewModel() {

    val appPreferences: StateFlow<fr.outadoc.justchatting.component.preferences.AppPreferences> =
        preferenceRepository.currentPreferences.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = fr.outadoc.justchatting.component.preferences.AppPreferences()
        )

    fun updatePreferences(appPreferences: fr.outadoc.justchatting.component.preferences.AppPreferences) {
        viewModelScope.launch {
            preferenceRepository.updatePreferences { appPreferences }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val currentUser = preferenceRepository.currentPreferences.first().appUser
            preferenceRepository.updatePreferences { current ->
                current.copy(appUser = fr.outadoc.justchatting.component.preferences.AppUser.NotLoggedIn)
            }

            try {
                val token = currentUser.helixToken
                if (!token.isNullOrBlank()) {
                    authRepository.revokeToken()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
